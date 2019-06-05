

package examples.linearRegression;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class LinearRegressionCSVAgentGUI extends JFrame {
	private LinearRegressionCSVAgent myAgent;

	private JTextField xField, yField;

	LinearRegressionCSVAgentGUI(LinearRegressionCSVAgent a) {
		super(a.getLocalName());

		myAgent = a;

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));

		p.add(new JLabel("X value:"));
		xField = new JTextField(15);
		p.add(xField);

		p.add(new JLabel("Y value:"));
		yField = new JTextField(15);
		p.add(yField);

		getContentPane().add(p, BorderLayout.CENTER);

		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
				    String x = xField.getText().trim();
					String y = yField.getText().trim();
					myAgent.updateCatalogue(Double.parseDouble(x), Double.parseDouble(y));
					xField.setText("");
					yField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(LinearRegressionCSVAgentGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);

		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );

		setResizable(false);
	}

	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
}
