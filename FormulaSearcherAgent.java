
package examples.linearRegression;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class FormulaSearcherAgent extends Agent {

	private String targetSubject;

	private AID[] linearRegressionAgents;


	protected void setup() {

		System.out.println("Hallo! FormulaSearcher "+getAID().getName()+" is ready.");


		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetSubject = (String) args[0];
			System.out.println("Target subject is "+targetSubject);


			addBehaviour(new TickerBehaviour(this, 7000) {
				protected void onTick() {
					System.out.println("Trying get " + targetSubject + " linear regression formula");

					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("linear-regression");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
						System.out.println("Found the following linear-regression agents:");
						linearRegressionAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							linearRegressionAgents[i] = result[i].getName();
							System.out.println(linearRegressionAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}


					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {

			System.out.println("No target subject specified");
			doDelete();
		}
	}


	protected void takeDown() {

		System.out.println("Formula-searcher-agent "+getAID().getName()+" terminating.");
	}


	private class RequestPerformer extends Behaviour {
		private AID mostReliable; 
		private int maxNumObs; 
		private int repliesCnt = 0; 
		private MessageTemplate mt;
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < linearRegressionAgents.length; ++i) {
					cfp.addReceiver(linearRegressionAgents[i]);
				}
				cfp.setContent(targetSubject);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
				myAgent.send(cfp);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						int numObs = Integer.parseInt(reply.getContent());
						if (mostReliable == null || numObs > maxNumObs) {
							maxNumObs = numObs;
							mostReliable = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= linearRegressionAgents.length) {
						step = 2;
					}
				}
				else {
					block();
				}
				break;
			case 2:
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(mostReliable);
				order.setContent(targetSubject);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:
				reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM) {
						System.out.println(targetSubject+" formula successfully gotted from agent "+reply.getSender().getName());
						System.out.println("Number of datos = "+maxNumObs);
						myAgent.doDelete();
					}
					else {
						System.out.println("Attempt failed: something went wrong.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && mostReliable == null) {
				System.out.println("Attempt failed: "+targetSubject+" data not available");
			}
			return ((step == 2 && mostReliable == null) || step == 4);
		}
	}
}
