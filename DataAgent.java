/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package winequality;

import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Daliabo
 */
public class DataAgent extends Agent {

    @Override
    protected void setup() {

        SendACLMessage(getDataFromCSV());

    }
//this method gets the data from csv file.
    private String getDataFromCSV() {
        
        //String builder to get the data as a String.
        StringBuilder stringbuilder = new StringBuilder();
        String filePath = "winequality-white.csv";
        //Stream to read the lines in the file.
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {

            stream.forEach((x) -> stringbuilder.append(x).append("\n"));

        } catch (IOException ex) {
        }
        //returns the data as String.
        return stringbuilder.toString();
    }
// this method sends the data to all agents.
    private void SendACLMessage(String data) {
        //creates a new ACLMessage of type INFORM.
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        
        //changes the data to byte[] to send it.
        byte[] b = data.getBytes();
        //sets the data in msg
        msg.setByteSequenceContent(b);
        //AMSAgentDesciotion to save info about all agents available on the plattform.
        AMSAgentDescription[] agents = null;
        try {
            //searches for all agents.
            SearchConstraints sc = new SearchConstraints();

            sc.setMaxResults(new Long(-1));
            //saves found agents in agents.
            agents = AMSService.search(this, new AMSAgentDescription(), sc);
        } catch (FIPAException ex) {
            Logger.getLogger("no agent found");
        }
        //for loop add all found agents as receivers.
        for (AMSAgentDescription agent : agents) {
            msg.addReceiver(agent.getName());
        }
        //sends the message.
        send(msg);

    }

}
