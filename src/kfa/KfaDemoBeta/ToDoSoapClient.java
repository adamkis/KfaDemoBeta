/**
 * 
 */
package kfa.KfaDemoBeta;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


/**
 * Creates a SOAP connection to the cloud of todos
 * the website is: kfademo.hostzi.com
 *
 */
public class ToDoSoapClient {

	static final String toDoServer = "http://kfademo.hostzi.com/todoSoap/todo_server.php";
	public Context toDoSoapActivityContext;
	
	
	/**
	 * Argumented constructor, to get the calling activity's context. 
	 * Purpose: making Toast from this class
	 * @param todoActivityContext
	 */
	public ToDoSoapClient(Context todoActivityContext){
		try {
			this.toDoSoapActivityContext = todoActivityContext.getApplicationContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Makes ToDosoap calls
	 * 		
	 * 		if mode is 'synch' -> Synchronizes the whole list. The master is the phone. The web content is just synchronized according to the phone's DB
	 * 		if mode is 'generateSid' -> gets Session ID
	 */
	public String doSoapCall(String inputString, String mode){
		
		final String toDoListSynchSoapCallHead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						"<SOAP-ENV:Envelope " +
							"xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
							"xmlns:ns1=\"urn:todo\" " +
							"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
							"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
							"xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" " +
							"SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
							
							"<SOAP-ENV:Body>" +
								"<ns1:refreshToDoDatabase>" +
									"<todoList xsi:type=\"xsd:string\">" ;
		final String toDoListSynchSoapCallTail =
									"</todoList>" +
								"</ns1:refreshToDoDatabase>" +
							"</SOAP-ENV:Body>" +
						"</SOAP-ENV:Envelope>";
		
		final String toDoListGetSIDSoapCallHead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						"<SOAP-ENV:Envelope " +
							"xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
							"xmlns:ns1=\"urn:todo\" " +
							"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
							"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
							"xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" " +
							"SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
						
							"<SOAP-ENV:Body>" +
								"<ns1:getSessionId>" +
									"<androiddeviceid xsi:type=\"xsd:string\">";
		final String toDoListGetSIDSoapCallTail =
									"</androiddeviceid>" +
								"</ns1:getSessionId>" +
							"</SOAP-ENV:Body>" +
						"</SOAP-ENV:Envelope>";
		
		String outputString = "";
		
		try {

			URL u = new URL(toDoServer);
			URLConnection uc = u.openConnection();
			HttpURLConnection connection = (HttpURLConnection) uc;
			  
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			  
			OutputStream out = connection.getOutputStream();
			Writer wout = new OutputStreamWriter(out);
			
			if( mode.equals("synch") )
				wout.write( toDoListSynchSoapCallHead + inputString + toDoListSynchSoapCallTail );
			
			else if( mode.equals("generateSid"))
				wout.write( toDoListGetSIDSoapCallHead + inputString + toDoListGetSIDSoapCallTail );
			
			else { Log.i("myDebug", "mode error" ); }
			
			wout.flush();
			wout.close();
			InputStream in = connection.getInputStream();
			
			// InputStream to String conversion
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = br.readLine()) != null) {
		    	sb.append(line);
		    } 
		    String responseString = sb.toString();
		    Log.i("myDebug" , "Complete  response:"+responseString);
		    
		    br.close();		  
		    in.close();
		    
		    if( mode.equals("synch") ){
		    	outputString = "Refresh successful";
		    }
		    else if( mode.equals("generateSid")){
		    	try{
			
		    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		            DocumentBuilder db = dbf.newDocumentBuilder();
		            Document doc = db.parse(new ByteArrayInputStream(responseString.getBytes("UTF-8")));
		            org.w3c.dom.Node child = doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild() ;
		            outputString = child.getFirstChild().getNodeValue();

		    	}
		    	catch(Exception e){
		    		
		    	}
		    }
		
		}
		catch (IOException e){
		  Log.i("myDebug" , e.toString());
		  Toast.makeText(toDoSoapActivityContext, "Ohh snap... Couldn't send the list to the cloud. Check your internet connection", Toast.LENGTH_LONG).show();
		}
		return outputString;
				
	}
	
	/**
	 * Builds XML that is sent to the SOAP server
	 * @param toDoItems The whole list of todos in an Arraylist of String[]{"id","todoText"} format. This is what the SQLite DB returns when fetched the whole list
	 * @param session_id The sessionId of the user using the app
	 * @return The URLEncoded String representation of the built XML
	 */
	public String buildTodoXML(ArrayList<String[]> toDoItems , String session_id){
		
		
		try{
			//We need a Document
	        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
	        Document toDoXML = docBuilder.newDocument();
	
			////////////////////////
			//Creating the XML tree
			
	        //create the root element and add it to the document
	        org.w3c.dom.Element toDoListRoot = toDoXML.createElement("todoList");
	        toDoXML.appendChild(toDoListRoot);
	        
			//Create TODO
	        org.w3c.dom.Element child = toDoXML.createElement("session_id");
			toDoListRoot.appendChild((Node) child);
			//add a text element to the child
			Text text = toDoXML.createTextNode(session_id);
			child.appendChild(text);
			  
			for(int i=0; i<toDoItems.size(); i++){
	        
				//Create TODO
				child = toDoXML.createElement("todotext");
				child.setAttribute("id", toDoItems.get(i)[0]);
				toDoListRoot.appendChild((Node) child);
				//add a text element to the child
				text = toDoXML.createTextNode(toDoItems.get(i)[1]);
				child.appendChild(text);
	        
	        }
	        
	        /////////////////
            //Output the XML

            //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "no");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(toDoXML);
            trans.transform(source, result);
            String xmlString = sw.toString();
            
            // UrlEncode the String
            String encodedString = URLEncoder.encode(xmlString, "UTF-8");

            Log.i("qw", encodedString);
            return encodedString;
	        
		}
		catch(Exception e){
			Log.i("myDebug", e.toString());
			Toast.makeText(toDoSoapActivityContext, "Ohh snap... Couldn't process the list", Toast.LENGTH_LONG).show();
			return e.toString();
		}
		
	}
	    
	    
}
