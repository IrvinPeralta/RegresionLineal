package examples.linearRegression;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;
import com.opencsv.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class LinearRegressionCSVAgent extends Agent {
	Random  rnd = new Random();
	ArrayList<Double> observationsX = new ArrayList<Double>();
	ArrayList<Double> observationsY = new ArrayList<Double>();
	String[] observation = new String[2];
	String fileName;
	String fileNamePre;
	CSVWriter writer;


	private LinearRegressionCSVAgentGUI myGui;


	protected void setup() {
		fileName = "C:\\Users\\IrvinPeralta\\Documents\\NetBeansProjects\\RegresionLineal\\src\\regresionlineal\\datos.csv";
        fileNamePre = "C:\\Users\\IrvinPeralta\\Documents\\NetBeansProjects\\RegresionLineal\\src\\regresionlineal\\prediccion.csv";


		myGui = new LinearRegressionCSVAgentGUI(this);
		myGui.showGui();


		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("linear-regression");
		sd.setName("CAYO-linear-regression");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}


		addBehaviour(new OfferRequestsServer());


		addBehaviour(new PurchaseOrdersServer());
	}


	protected void takeDown() {

		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		myGui.dispose();
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}

	public void updateCatalogue(final Double xValue, final Double yValue) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				System.out.println("Added observation X = " + xValue + " Y = " + yValue);
                observationsX.add(xValue);
                observationsY.add(yValue);

                try {
                    writer = new CSVWriter(new FileWriter(fileName, true));

                    observation[0] = Double.toString(xValue);
                    observation[1] = Double.toString(yValue);
                    writer.writeNext(observation);

                    writer.close();
				} catch (IOException e){
                    e.getMessage();
                }
			}
		} );
	}


	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String subject = msg.getContent();
				ACLMessage reply = msg.createReply();

				if (observationsX.size() > 0) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(observationsX.size()));
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  


	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();


                ArrayList<Double> xy = new ArrayList<Double>();
                ArrayList<Double> xx = new ArrayList<Double>();
                ArrayList<Double> yy = new ArrayList<Double>();
                Double sumX = 0.0;
                Double sumY = 0.0;
                Double sumXY = 0.0;
                Double sumXX = 0.0;
                Double sumYY = 0.0;
                Double a, b;
                Double max = observationsX.get(0);
                Double min = observationsX.get(0);
                Double diff;
                int random;

                int n = observationsX.size();

                for(int i = 0; i < n; i++){
                    xy.add(observationsX.get(i) * observationsY.get(i));
                    xx.add(observationsX.get(i) * observationsX.get(i));
                    yy.add(observationsY.get(i) * observationsY.get(i));

                    if(max < observationsX.get(i)){
                        max = observationsX.get(i);
                    }
                    if(min > observationsX.get(i)){
                        min = observationsX.get(i);
                    }
                }

                for(int i = 0; i < n; i++){
                    sumX += observationsX.get(i);
                    sumY += observationsY.get(i);
                    sumXY += xy.get(i);
                    sumXX += xx.get(i);
                    sumYY += yy.get(i);
                }
                System.out.println("sum x: " + sumX);
                System.out.println("sum y: " + sumY);
                System.out.println("sum xy: " + sumXY);
                System.out.println("sum xx: " + sumXX);
                System.out.println("sum yy: " + sumYY);

                a = ((sumY*sumXX) - (sumX*sumXY)) / ((n*sumXX)-(sumX*sumX));
                b = ((n*sumXY)-(sumX*sumY)) / ((n*sumXX)-(sumX*sumX));

                try {
                    writer = new CSVWriter(new FileWriter(fileNamePre, true));
                    diff = max - min;

                    for(int i = 0; i < 10; i++){
                        random = (int)(rnd.nextDouble() * (diff*2) + (min - diff/2));

                        observation[0] = Integer.toString(random);
                        observation[1] = Double.toString(a + b * random);
                        writer.writeNext(observation);
                    }

                    writer.close();
				} catch (IOException e){
                    e.getMessage();
                }

				if (a != null && b != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println("Formula: y = " + a + " + " + b + "x");
				}
				else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
}
