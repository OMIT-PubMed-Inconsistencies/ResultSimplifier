import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Nisansa on 11/21/2016.
 */
public class ResultSimplifier {
    ArrayList<String[]> premResults=new  ArrayList<String[]>();
    ArrayList<String> reduResults=new  ArrayList<String>();
    ArrayList<String> pubMedIds=new ArrayList<String>();
    boolean reDo=false;
    String olliePath="../output/03_Ollie/";
    HashMap<String,Node> rootNodes=new HashMap<String,Node>();
    OMITconnector oc=OMITconnector.getInstance();
    HashSet<String> remainingAbstracts=new HashSet<String>();
    int finalContradictionCount=0;

    public static void main(String[] args) {
        ResultSimplifier rs=new ResultSimplifier();
        rs.readFile();
        rs.readList();
        rs.expandResults();
        rs.writeResults();
    }

    private void writeResults() {
        System.out.println("Remaining abstract count: "+remainingAbstracts.size());
        System.out.println("Final contradiction count: "+finalContradictionCount);
        System.out.println("Writing file");
        PrintWriter writer = null;
        StringBuilder sb = null;
        try {
            writer = new PrintWriter("../output/reduResults.txt", "UTF-8");
            for (int i = 0; i < reduResults.size(); i++) {
                writer.println(reduResults.get(i));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private ArrayList<ArrayList<String>> readOllieFile(String path) throws Exception {
        ArrayList<ArrayList<String>> data=new ArrayList<ArrayList<String>>();
        ArrayList<String> sentence=new ArrayList<String>();

        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String s;
        while ((s = br.readLine()) != null) {
            if(s.isEmpty()) {
                data.add(sentence);
                sentence=new ArrayList<String>();
            }
            else{
                sentence.add(s);
            }
        }
        data.add(sentence);
        fr.close();

        return data;

    }

    private void readList(){
        if(reDo){
            File folder = new File("../output/02_Stanford");
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                //System.out.println();
                pubMedIds.add(listOfFiles[i].getName().split("\\.")[0]);
            }
        }
        else {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader("../pubmed-list.txt");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    //  System.out.println(line);
                    pubMedIds.add(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private void expandResults(){
        System.out.println("Expanding results");

        for (int i = 0; i <premResults.size() ; i++) {
            String[] parts=premResults.get(i);
            String a=parts[5]; //A from triplet where a triplet is (A;R;B)
            String b=parts[6]; //B from triplet where a triplet is (A;R;B)

            if(!doOMITCheck(a) && !doOMITCheck(b)){ //at least one should be an miRNA
                continue;
            }

            finalContradictionCount++;
            reduResults.add(parts[0]); //confidence


            reduResults.add("");
            reduResults.add(parts[1]); //id 1
            reduResults.add(parts[2]); //Date 1
            reduResults.add("( "+a+" ; "+parts[7]+" ; "+b+" )"); //Triple 1
            reduResults.add(parts[8]); //Sentence 1
            reduResults.add(getSentenceFromOllie(parts[1],parts[8]));
            remainingAbstracts.add(parts[1]);

            reduResults.add("");
            reduResults.add(parts[3]); //id 2
            reduResults.add(parts[4]); //Date 2
            reduResults.add("( "+a+" ; "+parts[9]+" ; "+b+" )"); //Triple 2
            reduResults.add(parts[10]); //Sentence 2
            reduResults.add(getSentenceFromOllie(parts[3],parts[10]));
            remainingAbstracts.add(parts[3]);


            reduResults.add("\n......................................................................................\n"); //seperator
           // System.out.println(i);
        }
    }

    private String getSentenceFromOllie(String pubMedID,String sentenceID){

        try {
            ArrayList<ArrayList<String>> OlleData=readOllieFile(olliePath+pubMedID);
            ArrayList<String> sentenceData=OlleData.get(Integer.parseInt(sentenceID));
            return sentenceData.get(0);
        } catch (Exception e) {

        }
        return "";
    }

    private boolean doOMITCheck(String s){
        Node n1=OMITcheck("&obo;NCRO_0000025",s);  //do a search in miRNA_target_gene "&obo;NCRO_0000025"
       Node n2=OMITcheck("&obo;NCRO_0000810",s); //do a search in human_miRNA ( "&obo;NCRO_0000810")
        return((n1!=null)||(n2!=null));
    }

    private Node OMITcheck(String root,String name){
        Node r=rootNodes.get(root);
        if(r==null){
            r=oc.getTreeRootedAt(root);
            rootNodes.put(root,r);
        }

        Node n=r.findNode(name);

        return (n);
    }

    private void readFile(){
        System.out.println("Reading result file");




        try {

            FileReader fileReader = new FileReader("../output/results.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;


            while ((line = bufferedReader.readLine()) != null) {
                premResults.add(line.split(";"));
                //System.out.println(line+" "+premResults.size());
            }
        } catch (Exception e) {
                e.printStackTrace();
        }

    }


    }


