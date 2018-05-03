import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.SoftTFIDF;

public class RecordLinkage {
	public static TreeMap<Integer, Record> source1;
	public static TreeMap<Integer, Record> source2;
	
	public static void main(String[] args) {
		////////////////////////////////////////
		args = new String[]{			
			"C:/uscProjects/548/hw7/zagat_secret.txt",
			"C:/uscProjects/548/hw7/fodors_secret.txt",		
			"output.txt"
		};		
		///////////////////////////////////////
		addstopWord();
		ArrayList<OutEntry> output = new ArrayList<OutEntry>();
		try{			
			source1 = readNodes(args[0]);
			source2 = readNodes(args[1]);
			
			//match(source1.get(91),source2.get(91));
			
			for(int r1 : source1.keySet()){
				//if(r1 > 113) break;
				boolean matched = false;
				for(int r2 : source2.keySet()){
					//if(r2 > 113) break;
					if(match(source1.get(r1),source2.get(r2))){
						output.add(new OutEntry(r1, r2));
						matched = true;
						//break;
					}
				}
				if(!matched){
					output.add(new OutEntry(r1, -1));
				}
			}				
			PrintWriter out = new PrintWriter(new FileOutputStream(args[2]));			
			for(OutEntry oe: output){
				oe.print(out);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	private static boolean match(Record r1, Record r2) {
		//TFIDF		
		String s1, s2;
		SoftTFIDF tfidf = new SoftTFIDF(new JaroWinkler(), 0.9);
	//////////////////////////////////////////////	
		s1 = preprocess(r1.name).toLowerCase();
		s2 = preprocess(r2.name).toLowerCase();		
			
		//System.out.println(s1);
		//System.out.println(s2);
		double measure = new Levenshtein().getSimilarity(s1, s2);			
		if(measure > 0.8){
			s2 = s1;
		}		
		else if(s1.startsWith(s2) || s1.endsWith(s2)){
			s2 = s1;
		}
		else if(s2.startsWith(s1) || s2.endsWith(s1)){
			s1 = s2;
		}
		else{
			String[] temp1 = s1.split("\\s+");
			String[] temp2 = s2.split("\\s+");
			int[] ct1 = new int[temp1.length];
			int[] ct2 = new int[temp2.length];
			
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();

			for(String s : stopwords){
				for(int i = 0; i < temp1.length; ++i){
					if(ct1[i] != -1 && temp1[i].equals(s)){
						ct1[i] = -1;
					}
				}
				for(int i = 0; i < temp2.length; ++i){
					if(ct2[i] != -1 && temp2[i].equals(s)){
						ct2[i] = -1;
					}
				}
			}
			for(int i = 0; i < temp1.length; ++i){
				if(ct1[i] != -1){				
					sb1.append(temp1[i]);
					if(i != (temp1.length - 1)){
						sb1.append(" ");
					}
				}
			}
			for(int i = 0; i < temp2.length; ++i){
				if(ct2[i] != -1){				
					sb2.append(temp2[i]);
					if(i != (temp2.length - 1)){
						sb2.append(" ");
					}
				}
			}
			String ss1 = sb1.toString();
			String ss2 = sb2.toString();

			//System.out.println(ss1);
			//System.out.println(ss2);

			//prefix or suffix
			if(!ss1.equals("") && !ss2.equals("") &&(ss1.startsWith(ss2) || ss1.endsWith(ss2))){
				s2 = s1;
			}
			if(!ss1.equals("") && !ss2.equals("") &&(ss2.startsWith(ss1) || ss2.endsWith(ss1))){
				s1 = s2;
			}
		}
		//abbr
		/*String[] temp;
		StringBuffer sb;

		temp = s1.split("\\s+");
		sb = new StringBuffer();
		
		for(int i = 0 ; i < temp.length; ++i){
			sb.append(temp[i].charAt(0));
		}
		if(sb.toString().equals(s2)){
			s2 = s1;
		}
		
		temp = s1.split("\\s+");
		sb = new StringBuffer();
		
		for(int i = 0 ; i < temp.length; ++i){
			sb.append(temp[i].charAt(0));
		}
		if(sb.toString().equals(s1)){
			s1 = s2;
		}*/

		double sname = tfidf.score( s1, s2);		

		s1 = preprocess(r1.address).toLowerCase();
		s2 = preprocess(r2.address).toLowerCase();
///////////////////////////////////////////////////
		String[] temp1 = s1.split("\\s+");
		String[] temp2 = s2.split("\\s+");
		
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();

		for(int i = 0; i < temp1.length; ++i){
			if(temp1[i].equals("rd")){
				sb1.append("road");
			}
			else if(temp1[i].equals("blv")){
				sb1.append("blvd");
			}
			else{
				sb1.append(temp1[i]);
			}
			
			if(i != (temp1.length - 1)){
				sb1.append(" ");
			}		
		}
		for(int i = 0; i < temp2.length; ++i){
			if(temp2[i].equals("rd")){
				sb2.append("road");
			}
			else if(temp2[i].equals("blv")){
				sb2.append("blvd");
			}
			else{
				sb2.append(temp2[i]);
			}
			
			if(i != (temp2.length - 1)){
				sb2.append(" ");
			}		
		}
		s1 = sb1.toString();
		s2 = sb2.toString();
		
		s1 = s1.toLowerCase().replaceAll("1st", "first");
		s1 = s1.toLowerCase().replaceAll("2nd", "second");
		s1 = s1.toLowerCase().replaceAll("3rd", "third");
		s1 = s1.toLowerCase().replaceAll("4th", "fourth");
		s1 = s1.toLowerCase().replaceAll("5th", "fifth");
		s1 = s1.toLowerCase().replaceAll("6th", "sixth");
		s1 = s1.toLowerCase().replaceAll("7th", "seventh");
		s1 = s1.toLowerCase().replaceAll("8th", "eight");
		s1 = s1.toLowerCase().replaceAll("9th", "ninth");

		s2 = s2.toLowerCase().replaceAll("1st", "first");
		s2 = s2.toLowerCase().replaceAll("2nd", "second");
		s2 = s2.toLowerCase().replaceAll("3rd", "third");
		s2 = s2.toLowerCase().replaceAll("4th", "fourth");
		s2 = s2.toLowerCase().replaceAll("5th", "fifth");
		s2 = s2.toLowerCase().replaceAll("6th", "sixth");
		s2 = s2.toLowerCase().replaceAll("7th", "seventh");
		s2 = s2.toLowerCase().replaceAll("8th", "eight");
		s2 = s2.toLowerCase().replaceAll("9th", "ninth");
						
		//prefix or suffix
		measure = new Levenshtein().getSimilarity(s1, s2);
		//System.out.println(measure);
		if(measure > 0.8){
			s2 = s1;
		}		
		else if(s1.startsWith(s2) || s1.endsWith(s2)){
			s2 = s1;
		}
		else if(s2.startsWith(s1) || s2.endsWith(s1)){
			s1 = s2;
		}
		double sadd = tfidf.score( s1,s2);

		s1 = preprocess(r1.city).toLowerCase();
		s2 = preprocess(r2.city).toLowerCase();
		//prefix or suffix
		if(s1.startsWith(s2) || s1.endsWith(s2)){
			s2 = s1;
		}
		else if(s2.startsWith(s1) || s2.endsWith(s1)){
			s1 = s2;
		}

		double scity = tfidf.score( s1, s2);		

		s1 = preprocess(r1.cuisine).toLowerCase();
		s2 = preprocess(r2.cuisine).toLowerCase();
		//prefix or suffix
		if(s1.startsWith(s2) || s1.endsWith(s2)){
			s2 = s1;
		}
		else if(s2.startsWith(s1) || s2.endsWith(s1)){
			s1 = s2;
		}
		double scuisine = tfidf.score(s1 ,s2);
		
		double score = sname*0.40 + sadd*0.45 + scity*0.10 + scuisine*0.05;
		//double score = sname*0.30 + sadd*0.35 + scity*0.20 + scuisine*0.15;
			
		//if(r1.id == r2.id && !(score > 0.8)){
			//System.out.println(sname + " " + sadd + " " + scity + " " + scuisine);
		//}
				
		return (score > 0.8);
	}
	private static String preprocess(String input){
		//remove punctuation		
		///////////////////////
		SnowballStemmer stemmer = new englishStemmer();				
		stemmer.setCurrent(input);				
		stemmer.stem();				
		String stripped = stemmer.getCurrent().replaceAll("\\p{Punct}+", " ");
		///////////////////////
		return stripped;
	}
	public static TreeMap<Integer, Record> readNodes(String file) throws IOException{
		TreeMap<Integer, Record> tree = new TreeMap<Integer, Record>();
		LineNumberReader rdr = new LineNumberReader(new FileReader(file));		
		String line = null;		
		
		while ((line = rdr.readLine()) != null) {
			if(!line.trim().equals("")){
				//System.out.println(line);
				String[] linedata = line.split("<<BRK>>");
				Record ent = new Record(); 
				ent.id = Integer.parseInt(linedata[0]);	
				ent.name = linedata[1];
				ent.address = linedata[2];
				ent.city = linedata[3];
				ent.cuisine = linedata[4];
				
				tree.put(ent.id, ent);				
			}
		}
		rdr.close();				
		return tree;
	}
	static class Record {
		public int id;
		public String name;
		public String address;
		public String city;
		public String cuisine;
	}

	static class OutEntry{	
		public int id1;
		public int id2;
		public OutEntry(int id1, int id2){
			this.id1 = id1;
			this.id2 = id2;
		}
		public void print(PrintWriter out){		
			System.
			out.println(id1 + " " + id2);
		}
	}
	
	public static ArrayList<String> stopwords = new ArrayList<String>();
	public static void add(String sw){
		stopwords.add(sw);
	}
	public static void addstopWord(){
	    add("a");
	    add("able");
	    add("about");
	    add("above");
	    add("according");
	    add("accordingly");
	    add("across");
	    add("actually");
	    add("after");
	    add("afterwards");
	    add("again");
	    add("against");
	    add("all");
	    add("allow");
	    add("allows");
	    add("almost");
	    add("alone");
	    add("along");
	    add("already");
	    add("also");
	    add("although");
	    add("always");
	    add("am");
	    add("among");
	    add("amongst");
	    add("an");
	    add("and");
	    add("another");
	    add("any");
	    add("anybody");
	    add("anyhow");
	    add("anyone");
	    add("anything");
	    add("anyway");
	    add("anyways");
	    add("anywhere");
	    add("apart");
	    add("appear");
	    add("appreciate");
	    add("appropriate");
	    add("are");
	    add("around");
	    add("as");
	    add("aside");
	    add("ask");
	    add("asking");
	    add("associated");
	    add("at");
	    add("available");
	    add("away");
	    add("awfully");
	    add("b");
	    add("be");
	    add("became");
	    add("because");
	    add("become");
	    add("becomes");
	    add("becoming");
	    add("been");
	    add("before");
	    add("beforehand");
	    add("behind");
	    add("being");
	    add("believe");
	    add("below");
	    add("beside");
	    add("besides");
	    add("best");
	    add("better");
	    add("between");
	    add("beyond");
	    add("both");
	    add("brief");
	    add("but");
	    add("by");
	    add("c");
	    add("came");
	    add("can");
	    add("cannot");
	    add("cant");
	    add("cause");
	    add("causes");
	    add("certain");
	    add("certainly");
	    add("changes");
	    add("clearly");
	    add("co");
	    add("com");
	    add("come");
	    add("comes");
	    add("concerning");
	    add("consequently");
	    add("consider");
	    add("considering");
	    add("contain");
	    add("containing");
	    add("contains");
	    add("corresponding");
	    add("could");
	    add("course");
	    add("currently");
	    add("d");
	    add("definitely");
	    add("described");
	    add("despite");
	    add("did");
	    add("different");
	    add("do");
	    add("does");
	    add("doing");
	    add("done");
	    add("down");
	    add("downwards");
	    add("during");
	    add("e");
	    add("each");
	    add("edu");
	    add("eg");
	    add("eight");
	    add("either");
	    add("else");
	    add("elsewhere");
	    add("enough");
	    add("entirely");
	    add("especially");
	    add("et");
	    add("etc");
	    add("even");
	    add("ever");
	    add("every");
	    add("everybody");
	    add("everyone");
	    add("everything");
	    add("everywhere");
	    add("ex");
	    add("exactly");
	    add("example");
	    add("except");
	    add("f");
	    add("far");
	    add("few");
	    add("fifth");
	    add("first");
	    add("five");
	    add("followed");
	    add("following");
	    add("follows");
	    add("for");
	    add("former");
	    add("formerly");
	    add("forth");
	    add("four");
	    add("from");
	    add("further");
	    add("furthermore");
	    add("g");
	    add("get");
	    add("gets");
	    add("getting");
	    add("given");
	    add("gives");
	    add("go");
	    add("goes");
	    add("going");
	    add("gone");
	    add("got");
	    add("gotten");
	    add("greetings");
	    add("h");
	    add("had");
	    add("happens");
	    add("hardly");
	    add("has");
	    add("have");
	    add("having");
	    add("he");
	    add("hello");
	    add("help");
	    add("hence");
	    add("her");
	    add("here");
	    add("hereafter");
	    add("hereby");
	    add("herein");
	    add("hereupon");
	    add("hers");
	    add("herself");
	    add("hi");
	    add("him");
	    add("himself");
	    add("his");
	    add("hither");
	    add("hopefully");
	    add("how");
	    add("howbeit");
	    add("however");
	    add("i");
	    add("ie");
	    add("if");
	    add("ignored");
	    add("immediate");
	    add("in");
	    add("inasmuch");
	    add("inc");
	    add("indeed");
	    add("indicate");
	    add("indicated");
	    add("indicates");
	    add("inner");
	    add("insofar");
	    add("instead");
	    add("into");
	    add("inward");
	    add("is");
	    add("it");
	    add("its");
	    add("itself");
	    add("j");
	    add("just");
	    add("k");
	    add("keep");
	    add("keeps");
	    add("kept");
	    add("know");
	    add("knows");
	    add("known");
	    add("l");
	    add("last");
	    add("lately");
	    add("later");
	    add("latter");
	    add("latterly");
	    add("least");
	    add("less");
	    add("lest");
	    add("let");
	    add("like");
	    add("liked");
	    add("likely");
	    add("little");
	    add("ll"); 
	    add("look");
	    add("looking");
	    add("looks");
	    add("ltd");
	    add("m");
	    add("mainly");
	    add("many");
	    add("may");
	    add("maybe");
	    add("me");
	    add("mean");
	    add("meanwhile");
	    add("merely");
	    add("might");
	    add("more");
	    add("moreover");
	    add("most");
	    add("mostly");
	    add("much");
	    add("must");
	    add("my");
	    add("myself");
	    add("n");
	    add("name");
	    add("namely");
	    add("nd");
	    add("near");
	    add("nearly");
	    add("necessary");
	    add("need");
	    add("needs");
	    add("neither");
	    add("never");
	    add("nevertheless");
	    add("new");
	    add("next");
	    add("nine");
	    add("no");
	    add("nobody");
	    add("non");
	    add("none");
	    add("noone");
	    add("nor");
	    add("normally");
	    add("not");
	    add("nothing");
	    add("novel");
	    add("now");
	    add("nowhere");
	    add("o");
	    add("obviously");
	    add("of");
	    add("off");
	    add("often");
	    add("oh");
	    add("ok");
	    add("okay");
	    add("old");
	    add("on");
	    add("once");
	    add("one");
	    add("ones");
	    add("only");
	    add("onto");
	    add("or");
	    add("other");
	    add("others");
	    add("otherwise");
	    add("ought");
	    add("our");
	    add("ours");
	    add("ourselves");
	    add("out");
	    add("outside");
	    add("over");
	    add("overall");
	    add("own");
	    add("p");
	    add("particular");
	    add("particularly");
	    add("per");
	    add("perhaps");
	    add("placed");
	    add("please");
	    add("plus");
	    add("possible");
	    add("presumably");
	    add("probably");
	    add("provides");
	    add("q");
	    add("que");
	    add("quite");
	    add("qv");
	    add("r");
	    add("rather");
	    add("rd");
	    add("re");
	    add("really");
	    add("reasonably");
	    add("regarding");
	    add("regardless");
	    add("regards");
	    add("relatively");
	    add("respectively");
	    add("right");
	    add("s");
	    add("said");
	    add("same");
	    add("saw");
	    add("say");
	    add("saying");
	    add("says");
	    add("second");
	    add("secondly");
	    add("see");
	    add("seeing");
	    add("seem");
	    add("seemed");
	    add("seeming");
	    add("seems");
	    add("seen");
	    add("self");
	    add("selves");
	    add("sensible");
	    add("sent");
	    add("serious");
	    add("seriously");
	    add("seven");
	    add("several");
	    add("shall");
	    add("she");
	    add("should");
	    add("since");
	    add("six");
	    add("so");
	    add("some");
	    add("somebody");
	    add("somehow");
	    add("someone");
	    add("something");
	    add("sometime");
	    add("sometimes");
	    add("somewhat");
	    add("somewhere");
	    add("soon");
	    add("sorry");
	    add("specified");
	    add("specify");
	    add("specifying");
	    add("still");
	    add("sub");
	    add("such");
	    add("sup");
	    add("sure");
	    add("t");
	    add("take");
	    add("taken");
	    add("tell");
	    add("tends");
	    add("th");
	    add("than");
	    add("thank");
	    add("thanks");
	    add("thanx");
	    add("that");
	    add("thats");
	    add("the");
	    add("their");
	    add("theirs");
	    add("them");
	    add("themselves");
	    add("then");
	    add("thence");
	    add("there");
	    add("thereafter");
	    add("thereby");
	    add("therefore");
	    add("therein");
	    add("theres");
	    add("thereupon");
	    add("these");
	    add("they");
	    add("think");
	    add("third");
	    add("this");
	    add("thorough");
	    add("thoroughly");
	    add("those");
	    add("though");
	    add("three");
	    add("through");
	    add("throughout");
	    add("thru");
	    add("thus");
	    add("to");
	    add("together");
	    add("too");
	    add("took");
	    add("toward");
	    add("towards");
	    add("tried");
	    add("tries");
	    add("truly");
	    add("try");
	    add("trying");
	    add("twice");
	    add("two");
	    add("u");
	    add("un");
	    add("under");
	    add("unfortunately");
	    add("unless");
	    add("unlikely");
	    add("until");
	    add("unto");
	    add("up");
	    add("upon");
	    add("us");
	    add("use");
	    add("used");
	    add("useful");
	    add("uses");
	    add("using");
	    add("usually");
	    add("uucp");
	    add("v");
	    add("value");
	    add("various");
	    add("ve"); //added to avoid words like I've,you've etc.
	    add("very");
	    add("via");
	    add("viz");
	    add("vs");
	    add("w");
	    add("want");
	    add("wants");
	    add("was");
	    add("way");
	    add("we");
	    add("welcome");
	    add("well");
	    add("went");
	    add("were");
	    add("what");
	    add("whatever");
	    add("when");
	    add("whence");
	    add("whenever");
	    add("where");
	    add("whereafter");
	    add("whereas");
	    add("whereby");
	    add("wherein");
	    add("whereupon");
	    add("wherever");
	    add("whether");
	    add("which");
	    add("while");
	    add("whither");
	    add("who");
	    add("whoever");
	    add("whole");
	    add("whom");
	    add("whose");
	    add("why");
	    add("will");
	    add("willing");
	    add("wish");
	    add("with");
	    add("within");
	    add("without");
	    add("wonder");
	    add("would");
	    add("would");
	    add("x");
	    add("y");
	    add("yes");
	    add("yet");
	    add("you");
	    add("your");
	    add("yours");
	    add("yourself");
	    add("yourselves");
	    add("z");
	    add("zero");
	  }
}
