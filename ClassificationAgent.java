/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package winequality;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author Daliabo
 */
public class ClassificationAgent extends Agent {

    //creates two states which the agent can be in
    private static final String STATE_1 = "getMessage";
    private static final String STATE_2 = "REngine";

    @Override
    protected void setup() {
        //creates FSMBehaviour.
        FSMBehaviour fsm = new FSMBehaviour(this);
        //binds the state with behaviour
        fsm.registerFirstState(new getMessage(), STATE_1);
        fsm.registerLastState(new REngine(), STATE_2);
        //tells in which order the states will be excuted.
        fsm.registerDefaultTransition(STATE_1, STATE_2);
        //adds the behaviour.
        addBehaviour(fsm);

    }

    //this behaviour gets the message from the other agent.
    private class getMessage extends OneShotBehaviour {

        @Override
        public void action() {

            //creates ACLMessage.
            ACLMessage msg = blockingReceive();//blocks the agent from working until the message is received.
            if (msg != null) {
                byte[] data = msg.getByteSequenceContent();

                //saves the received data with fileWriter and creates a new csv file.                
                String filePath = "receivedData.csv";

                try {
                    FileWriter fileWriter = new FileWriter(filePath, false);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    PrintWriter printWriter = new PrintWriter(bufferedWriter);

                    String s = new String(data);

                    printWriter.println(s);

                    printWriter.flush();
                    printWriter.close();

                } catch (IOException ex) {
                }
                //end try.
            }

        }
    }

    //this behaviour finds the created csv file reads it and uses REngine to calculate the results.
    private class REngine extends OneShotBehaviour {

        @Override
        public void action() {

            try {

                //Creates a connection to R to send data.
                RConnection c = new RConnection();
                REXP rexp = null;
                //r code to get the confusion tabel and calculate accuracy and precision.
                //REXP gets the results from R.
                rexp = c.eval("wineQualityData <- read.csv2('C:/Users/Daliabo/OneDrive/doc/NetBeansProjects/WineQualityV3~/WineQuality/receivedData.csv', header = TRUE)\n"
                        + "library(class) #is needed to excute methods in r\n"
                        + "set.seed(1234) #needed for nomalization to work properly and to ensure randomness\n"
                        + "#spliting WineQualityData into training- and test data.\n"
                        + "idx <- sample(2, nrow(wineQualityData), replace=TRUE, prob=c(2/3, 1/3))\n"
                        + "#creates vectors for the test- and training data.\n"
                        + "wineQualityData.training <- wineQualityData[idx==1, 1:11]\n"
                        + "wineQualityData.test <- wineQualityData[idx==2, 1:11] \n"
                        + "#creates labels or targetvalues for train and test.\n"
                        + "wineQualityData.trainLabels <- wineQualityData[idx==1, 12]\n"
                        + "wineQualityData.testLabels <- wineQualityData[idx==2, 12]\n"
                        + "#gets the classified data with the use of knn.\n"
                        + "classifiedData <- knn(train = wineQualityData.training, test = wineQualityData.test, cl = wineQualityData.trainLabels, k=5)\n"
                        + "#creates the confusion tabel.\n"
                        + "confusionTabel <- table(Target = wineQualityData.testLabels, Predicted = classifiedData)\n"
                        + "#Calculate the accuracy.\n"
                        + "accuracy <- (sum(diag(confusionTabel))/sum(confusionTabel) )* 100\n"
                        + "#Calculate the precision.\n"
                        + "precision <- sum(diag(confusionTabel)/rowSums(confusionTabel))/nrow(confusionTabel)\n"
                        + "capture.output({confusionTabel},{accuracy},{precision})"
                );
              
                //gets the results and saves is as String array.
                String[] results = rexp.asStrings();

                //writes out the results.
                for (String string : results) {

                    System.out.println(string);

                }

            } catch (RserveException | REXPMismatchException ex) {
                Logger.getLogger(ClassificationAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
