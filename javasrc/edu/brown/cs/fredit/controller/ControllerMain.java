/********************************************************************************/
/*										*/
/*		ControllerMain.java						*/
/*										*/
/*	description of class							*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.fredit.controller;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.fresh.FreshManager;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSafetyCondition;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSkipItem;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSubtype;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSubtypeValue;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.exec.IvyExecQuery;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.mint.MintArguments;
import edu.brown.cs.ivy.mint.MintConstants;
import edu.brown.cs.ivy.mint.MintControl;
import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.mint.MintHandler;
import edu.brown.cs.ivy.mint.MintMessage;
import edu.brown.cs.ivy.mint.MintReply;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class ControllerMain implements ControllerConstants, MintConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String mint_id;
private String workspace_id;
private boolean using_bubbles;
private MintControl mint_control;
private String eclipse_dir;
private String library_dir;
private String props_dir;
private int    num_threads;
private Element last_analysis;
private ControllerPanel control_panel;
private FreshManager file_manager;
// private List<ControllerResourceFile> resource_files;
private WindowCreator window_creator;
private String session_id;
private String result_id;

private static Random random_gen = new Random();
private static int random_int = random_gen.nextInt(1000000);

private static final String SOURCE_ID = "FREDIT_" + random_int;
private static final String RESULT_ID = "RETURN_" + random_int;

private static boolean do_debug = true;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ControllerMain(String [] args,WindowCreator wc)
{
   mint_id = null;
   workspace_id = null;
   mint_control = null;
   eclipse_dir = null;
   library_dir = null;
   num_threads = 4;
   last_analysis = null;
   control_panel = null;
   window_creator = wc;
   session_id = SOURCE_ID;
   result_id = RESULT_ID;
   
   file_manager = new FreshManager();

   File f = new File(System.getProperty("user.home"));
   File f1 = new File(f,".bubbles");;
   props_dir = f1.getAbsolutePath();
   
   scanArgs(args);
   
   using_bubbles = mint_id != null && workspace_id == null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getWorkspaceName()
{
   String wsname = workspace_id;
   int idx = wsname.lastIndexOf(File.separator);
   if (idx > 0) wsname = wsname.substring(idx+1);
   return wsname;
}


public String getTraceId()
{
   return RESULT_ID;
}


public String getSessionId()
{
   return session_id;
}

public void addEditor(ControllerEditor ed)
{
   if (control_panel == null) setup();
   control_panel.addTab(ed.getName(),ed.getPanel());
}


public JPanel getPanel()
{
   if (control_panel == null) setup();
   return control_panel.getPanel();
}

public void displayWindow(String ttl,JComponent contents)
{
   window_creator.createWindow(ttl,contents);
}


public static Component getDisplayParent(Component pane)
{
   Class<?> bbl = null;
   try {
      bbl = Class.forName("edu.brown.cs.bubbles.BudaBubble");
    }
   catch (ClassNotFoundException e) { }
   
   for (Component c = pane; c != null; c = c.getParent()) {
      if (bbl != null && bbl.isAssignableFrom(c.getClass())) return c;
      if (c instanceof JDialog) return c;
      if (c instanceof JFrame) return c;
    }
   return pane;
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public void setup()
{
   setupBedrockAndFait();

   if (last_analysis == null) startAnalysis();

   getResourceFilesFromFait();

   control_panel = new ControllerPanel(this);
}


public void recordFaitAnalysis(Element xml)
{
   receiveAnalysis(xml);
}



/********************************************************************************/
/*										*/
/*	Argument scanning							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-m") && i+1 < args.length) {           // -m <mint id>
	    mint_id = args[++i];
	  }
	 else if (args[i].startsWith("-w") && i+1 < args.length) {      // -w <workspace>
	    workspace_id = args[++i];
	  }
	 else if (args[i].startsWith("-e") && i+1 < args.length) {      // -e <eclipsepath>
	    eclipse_dir = args[++i];
	  }
	 else if (args[i].startsWith("-p") && i+1 < args.length) {      // -p <props directory>
	    props_dir = args[++i];
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {      // -t <# threads>
	    num_threads = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-s") && i+1 < args.length) {      // -s <Session id>
	    session_id = args[++i];
	  }
	 else if (args[i].startsWith("-r") && i+1 < args.length) {      // -r <Result id>
	    result_id = args[++i];
	  }
	 else badArgs();
       }
      else badArgs();
    }
}



private void badArgs()
{
   System.err.println("FREDIT: fredit [-m <mintid>] [-w <workspace path>]");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Mint and Bedrock setup methods						*/
/*										*/
/********************************************************************************/


private void setupBedrockAndFait()
{
   if (usingBubbles()) {
      if (!connectToBubbles()) {
	 System.err.println("FREDIT: Fredit must be running with only the -m option");
	 System.exit(1);
       }
      return;
    }

   String wsname = null;
   if (workspace_id != null) {
      wsname = workspace_id;
      int idx = wsname.lastIndexOf(File.separator);
      if (idx > 0) wsname = wsname.substring(idx+1);
      wsname = wsname.replace(" ","_");
    }
   String mint = "BUBBLES_" + System.getProperty("user.name").replace(" ","_") + "_" + wsname;

   if (mint_id != null && workspace_id == null) {
      if (connectToBubbles()) {
	 return;
       }
    }

   if (workspace_id != null) {
      mint_id = mint;
      File f = new File(workspace_id);
      if (f.exists() && f.isDirectory() && f.canRead()) {
	 startBedrock();
	 startFait();
       }
    }
   
   CommandArgs cargs = null;
   Element xml= sendFaitReply(session_id,"BEGIN",cargs,null);
   if (xml == null) {
      System.err.println("FREDIT: Can't start fait analysis");
      System.exit(1);
    }
   
   addUserFiles();
}



private void startBedrock()
{
   startMint();

   Properties sysprops = loadProperites("System");
   if (eclipse_dir == null && props_dir != null) {
      String prop =  "edu.brown.cs.bubbles.baseide." + System.getProperty("os.arch");
      eclipse_dir = getProperty(sysprops,prop);
    }
   if (eclipse_dir == null) {
      System.err.println("FREDIT: Bubbles not running and can't find eclispe");
      System.exit(1);
    }
   File ed = new File(eclipse_dir);
   File ebin = null;
   for (String s : new String [] { "eclipse", "eclipse.exe", "Eclipse.app" }) {
      File ef1 = new File(ed,s);
      if (ef1.exists() && ef1.canExecute()) {
         if (ef1.isDirectory()) {
            File ef2 = new File(ef1,"Contents");
            ef1 = null;
            for (String s1 : new String [] { "MacOS", "Eclipse" }) {
               File ef3 = new File(ef2,s1);
               File ef4 = new File(ef3,"eclipse");
               if (ef4.exists() && ef4.canExecute()) {
                  ef1 = ef4;
                  break;
                }
             }
          }
         if (ef1 != null) {
            ebin = ef1;
            break;
          }
       }
      ef1 = null;
    }
   if (ebin == null) {
      System.err.println("FREDIT: Bubbles not running and can't find eclipse binary");
      System.exit(1);
    }

   String inst = getProperty(sysprops,"edu.brown.cs.bubbles.install");
   if (inst == null) inst = getProperty(sysprops,"edu.brown.cs.bubbles.jar");
   if (inst != null) {
      File f1 = new File(inst);
      File f2 = new File(f1,"lib");
      if (f2.exists() && f2.isDirectory()) library_dir = f2.getAbsolutePath();
    }


   File ec2 = new File(workspace_id);
   String cmd = ebin.getAbsolutePath();
   cmd += " -application edu.brown.cs.bubbles.bedrock.application";
   cmd += " -data " + ec2.getAbsolutePath();
   cmd += " -bhide";
   cmd += " -nosplash";
   cmd += " -vmargs -Dedu.brown.cs.bubbles.MINT=" + mint_id;
   // cmd += " -Xdebug -Xrunjdwp:transport=dt_socket,address=32328,server=y,suspend=n";
   // cmd += " -Xmx16000m";

   IvyLog.logD("FREDIT","RUN: " + cmd);

   try {
      for (int i = 0; i < 250; ++i) {
	 if (pingEclipse()) {
	    CommandArgs args = new CommandArgs("LEVEL","DEBUG");
	    sendBubblesMessage("LOGLEVEL",null,args,null);
	    sendBubblesMessage("ENTER");
	    Element projsxml = sendBubblesXmlReply("PROJECTS",null,null,null);
	    if (!IvyXml.isElement(projsxml,"RESULT")) {
	       System.err.println("FREDIT: Problem starting bedrock -- no projects");
	       System.exit(1);
	     }
	    for (Element pxml : IvyXml.children(projsxml,"PROJECT")) {
	       String pname = IvyXml.getAttrString(pxml,"NAME");
	       Element rxml = sendBubblesXmlReply("OPENPROJECT",pname,null,null);
	       if (!IvyXml.isElement(rxml,"RESULT")) {
		  System.err.println("FREDIT: Problem opening project " + pname);
		  System.exit(1);
		}
	     }
	    Runtime.getRuntime().addShutdownHook(new BedrockExiter());
	    return;
	  }
	 if (i == 0) new IvyExec(cmd);
	 else {
	    try { Thread.sleep(100); } catch (InterruptedException e) { }
	  }
       }
    }
   catch (IOException e) { }

   throw new Error("Problem running Eclipse: " + cmd);
}



private void startFait()
{
   if (do_debug) {
      String [] argv = new String [] { "-m", mint_id, "-DEBUG", "-TRACE",
	    "-LOG", "/vol/spr/fredittest.log" };
      edu.brown.cs.fait.server.ServerMain.main(argv);
      System.err.println("FREDIT: Fait log in /vol/spr/freditest.log");
      return;
    }

   Properties props = loadProperites("Bsean");
   String dbgargs = getProperty(props,"Bsean.jvm.args");

   List<String> args = new ArrayList<>();
   args.add(IvyExecQuery.getJavaPath());

   if (dbgargs != null && dbgargs.contains("###")) {
      int port = (int)(Math.random() * 1000 + 3000);
      dbgargs = dbgargs.replace("###",Integer.toString(port));
    }
   if (dbgargs != null) {
      StringTokenizer tok = new StringTokenizer(dbgargs);
      while (tok.hasMoreTokens()) {
	 args.add(tok.nextToken());
       }
    }

   String xcp = getProperty(props,"Bsean.fait.class.path");
   if (xcp == null) {
      xcp = System.getProperty("java.class.path");
      String ycp = getProperty(props,"Bsean.fait.add.path");
      if (ycp != null) xcp = ycp + File.pathSeparator + xcp;
    }
   else {
      StringBuffer buf = new StringBuffer();
      StringTokenizer tok = new StringTokenizer(xcp,":;");
      while (tok.hasMoreTokens()) {
	 String elt = tok.nextToken();
	 if (!elt.startsWith("/") &&  !elt.startsWith("\\")) {
	    if (elt.equals("eclipsejar")) {
	       File ejr = getLibraryPath(elt);
	       if (ejr.exists() && ejr.isDirectory()) {
		  for (File nfil : ejr.listFiles()) {
		     if (nfil.getName().startsWith("org.eclipse.") && nfil.getName().endsWith(".jar")) {
			if (buf.length() > 0) buf.append(File.pathSeparator);
			buf.append(nfil.getPath());
		      }
		   }
		}
	       continue;
	     }
	    else {
	       elt = getLibraryPath(elt).getAbsolutePath();
	     }
	  }
	 if (buf.length() > 0) buf.append(File.pathSeparator);
	 buf.append(elt);
       }
      xcp = buf.toString();
    }

   args.add("-cp");
   args.add(xcp);
   args.add("edu.brown.cs.fait.iface.FaitMain");
   args.add("-m");
   args.add(mint_id);

   IvyExec exec = null;
   boolean running = false;
   for (int i = 0; i < 100; ++i) {
      MintDefaultReply rply = new MintDefaultReply();
      mint_control.send("<FAIT DO='PING' SID='*' />",rply,MINT_MSG_FIRST_NON_NULL);
      String rslt = rply.waitForString(1000);
      if (rslt != null) {
	 running = true;
	 break;
       }
      if (i == 0) {
	 try {
	    exec = new IvyExec(args,null,IvyExec.ERROR_OUTPUT);     // make IGNORE_OUTPUT to clean up otuput
	  }
	 catch (IOException e) {
	    break;
	  }
       }
      else {
	 try {
	    if (exec != null) {
	       exec.exitValue();
	       IvyLog.logD("FREDIT","Fait terminated");
	       running = false;
	       break;
	     }
	  }
	 catch (IllegalThreadStateException e) { }
       }

      try {
	 Thread.sleep(1000);
       }
      catch (InterruptedException e) { }
    }

   if (!running) {
      System.err.println("FREDIT: Couldn't start fait server");
      System.exit(1);
    }
}



private void addUserFiles()
{
   IvyXmlWriter xw = new IvyXmlWriter();

   Element xml = sendBubblesXmlReply("PROJECTS",null,null,null);
   for (Element pxml : IvyXml.children(xml,"PROJECT")) {
      if (IvyXml.getAttrBool(pxml,"ISJAVA")) {
         String name = IvyXml.getAttrString(pxml,"NAME");
         CommandArgs args = new CommandArgs("FILES",true);
         Element fxml = sendBubblesXmlReply("OPENPROJECT",name,args,null);
         fxml = IvyXml.getChild(fxml,"PROJECT");
         fxml = IvyXml.getChild(fxml,"FILES");
         for (Element file : IvyXml.children(fxml,"FILE")) {
            if (IvyXml.getAttrBool(file,"SOURCE")) {
               File f2 = new File(IvyXml.getText(file));
               if (f2.exists() && f2.getName().endsWith(".java")) {
                  f2 = IvyFile.getCanonical(f2);
                  xw.begin("FILE");
                  xw.field("NAME",f2.getPath());
                  xw.end("FILE");
                }
             }
          }
       }
    }
   
   sendFaitReply(session_id,"ADDFILE",null,xw.toString());
   
   xw.close();
}




/********************************************************************************/
/*										*/
/*	Resource file methods   						*/
/*										*/
/********************************************************************************/

private void getResourceFilesFromFait()
{
   waitForAnalysis();
   
   Element xml = sendFaitReply(session_id,"RESOURCES",null,null);
   if (xml == null) {
      System.err.println("FREDIT: Can't get resource files");
      System.exit(1);
    }
   file_manager.setupResourceFiles(xml);
}


public Collection<FreshSkipItem> getSkippedItems()
{
   return file_manager.getSkippedItems();
}


public Collection<FreshSubtype> getSubtypes()
{
   return file_manager.getSubtypes();
}


public FreshSubtype createSubtype(String name)
{
   return file_manager.createSubtype(name); 
}


public FreshSubtypeValue createSubtypeValue(String name)
{
   return file_manager.createSubtypeValue(name); 
}


public Collection<FreshSafetyCondition> getSafetyConditions()
{
   return file_manager.getSafetyConditions();
}


public FreshSafetyCondition createSafetyCondition(String name)
{
   return file_manager.createSafetyCondition(name);
}



/********************************************************************************/
/*                                                                              */
/*      Analysis methods                                                        */
/*                                                                              */
/********************************************************************************/

private void startAnalysis()
{
   String typ = "FULL_STATS";
   CommandArgs cargs = new CommandArgs("ID",result_id,"THREADS",num_threads,"REPORT",typ);
   Element xml = sendFaitReply(session_id,"ANALYZE",cargs,null);
   if (xml == null) {
      System.err.println("FREDIT: Can't start fait analysis");
      System.exit(1);
    }
}



public synchronized Element waitForAnalysis()
{
   for ( ; ; ) {
      if (last_analysis != null) return last_analysis;
      try {
	 wait(1000);
       }
      catch (InterruptedException e) {}
    }
}


private synchronized void receiveAnalysis(Element rslt)
{
   last_analysis = rslt;
   notifyAll();
}



/********************************************************************************/
/*										*/
/*	Property methods							*/
/*										*/
/********************************************************************************/

private Properties loadProperites(String nm)
{
   Properties p = new Properties();
   File p1 = new File(props_dir);
   File p2 = new File(p1,nm + ".props");
   try {
      FileInputStream fis = new FileInputStream(p2);
      p.loadFromXML(fis);
      fis.close();
    }
   catch (IOException e) {
      IvyLog.logE("FREDIT","Problem loading properties: " + e);
    }
   return p;
}




private String getProperty(Properties props,String nm)
{
   if (props == null) return null;

   String arch = System.getProperty("os.arch");
   String v = props.getProperty(nm + "." + arch);
   if (v == null) v = props.getProperty(nm);
   return v;
}


private File getLibraryPath(String name)
{
   File f1 = new File(library_dir);
   File f2 = new File(f1,name);
   if (name.equals("eclipsejar") && !f2.exists()) {
      File f3 = f1.getParentFile();
      f2 = new File(f3,name);
    }
   return f2;
}



/********************************************************************************/
/*										*/
/*	Bubbles connection methods						*/
/*										*/
/********************************************************************************/

private void startMint()
{
   if (mint_control != null) return;
   mint_control = MintControl.create(mint_id,MintSyncMode.ONLY_REPLIES);

   mint_control.register("<BEDROCK SOURCE='ECLIPSE' TYPE='_VAR_0' />",new ControllerEclipseHandler());
   mint_control.register("<FAITEXEC TYPE='_VAR_0' />",new FaitHandler());
}


private boolean connectToBubbles()
{
   startMint();
   if (!pingEclipse()) return false;

   Element xml = sendBubblesXmlReply("PROJECTS",null,null,null);
   if (!IvyXml.isElement(xml,"RESULT")) return false;
   for (Element pxml : IvyXml.children(xml,"PROJECT")) {
      if (IvyXml.getAttrBool(pxml,"ISJAVA")) {
	 String ws = IvyXml.getAttrString(pxml,"WORKSPACE");
	 workspace_id = ws;
       }
    }

   return true;
}



private boolean pingEclipse()
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendBubblesMessage("PING",null,null,null,mdr);
   String r = mdr.waitForString(500);
   return r != null;
}


public boolean usingBubbles()
{
   return using_bubbles;
}


/********************************************************************************/
/*										*/
/*	Bubbles communication methods						*/
/*										*/
/********************************************************************************/


private Element sendBubblesXmlReply(String cmd,String proj,Map<String,Object> flds,String cnts)
{
   MintDefaultReply mdr = new MintDefaultReply();
   sendBubblesMessage(cmd,proj,flds,cnts,mdr);
   Element pxml = mdr.waitForXml();
   return pxml;
}



private void sendBubblesMessage(String cmd)
{
   sendBubblesMessage(cmd,null,null,null,null);
}


private void sendBubblesMessage(String cmd,String proj,Map<String,Object> flds,String cnts)
{
   sendBubblesMessage(cmd,proj,flds,cnts,null);
}


private void sendBubblesMessage(String cmd,String proj,Map<String,Object> flds,String cnts,
      MintReply rply)
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("BUBBLES");
   xw.field("DO",cmd);
   xw.field("BID",SOURCE_ID);
   if (proj != null && proj.length() > 0) xw.field("PROJECT",proj);
   if (flds != null) {
      for (Map.Entry<String,Object> ent : flds.entrySet()) {
	 xw.field(ent.getKey(),ent.getValue());
       }
    }
   xw.field("LANG","eclipse");
   if (cnts != null) xw.xmlText(cnts);
   xw.end("BUBBLES");

   String xml = xw.toString();
   xw.close();

   int fgs = MINT_MSG_NO_REPLY;
   if (rply != null) fgs = MINT_MSG_FIRST_NON_NULL;
   mint_control.send(xml,rply,fgs);
}



public Element sendFaitReply(String sid,String cmd,CommandArgs args,String xml)
{
   MintDefaultReply rply = new MintDefaultReply();
   sendFait(sid,cmd,args,xml,rply);
   Element rslt = rply.waitForXml();
   return rslt;
}



private void sendFait(String sid,String cmd,CommandArgs args,String xml,MintReply rply)
{
   IvyXmlWriter msg = new IvyXmlWriter();
   msg.begin("FAIT");
   msg.field("DO",cmd);
   msg.field("SID",sid);
   if (args != null) {
      for (Map.Entry<String,Object> ent : args.entrySet()) {
	 if (ent.getValue() == null) continue;
	 msg.field(ent.getKey(),ent.getValue().toString());
       }
    }
   if (xml != null) msg.xmlText(xml);
   msg.end("FAIT");
   String msgt = msg.toString();
   msg.close();
   IvyLog.logD("FREDIT","Send to FAIT: " + msg);

   if (rply == null) {
      mint_control.send(msgt,rply,MintConstants.MINT_MSG_NO_REPLY);
    }
   else {
      mint_control.send(msgt,rply,MintConstants.MINT_MSG_FIRST_NON_NULL);
    }
}




/********************************************************************************/
/*										*/
/*	Message handler 							*/
/*										*/
/********************************************************************************/

private class ControllerEclipseHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      switch (cmd) {
	 case "PING" :
	    msg.replyTo("<PONG/>");
	    break;
	 case "ELISION" :
	 case "RESOURCE" :
	    break;
	 default :
	    msg.replyTo();
	    break;
       }
    }

}	// end of inner class ControllerEclipseHandler



private class FaitHandler implements MintHandler {

   @Override public void receive(MintMessage msg,MintArguments args) {
      String cmd = args.getArgument(0);
      Element xml = msg.getXml();
      IvyLog.logD("FREDIT","Received from FAIT: " + IvyXml.convertXmlToString(xml));
   
      switch (cmd) {
         case "ANALYSIS" :
            if (!IvyXml.getAttrBool(xml,"STARTED")) {
               String rid = IvyXml.getAttrString(xml,"ID");
               if (rid.equals(RESULT_ID) && !IvyXml.getAttrBool(xml,"ABORTED")) {
                  receiveAnalysis(xml);
                }
             }
            msg.replyTo();
            break;
         case "PING" :
            msg.replyTo("<PONG/>");
            break;
         default :
            msg.replyTo();
            break;
       }
    }

}	// end of inner class FaitHandler



/********************************************************************************/
/*										*/
/*	Handle termination							*/
/*										*/
/********************************************************************************/

private class BedrockExiter extends Thread {

   BedrockExiter() {
      super("BedrockExiter");
    }

   @Override public void run() {
      IvyLog.logD("FREDIT","SHUT DOWN BEDROCK");
      sendBubblesMessage("EXIT");
      mint_control = null;
    }

}	// end of inner class BedrockExiter




}	// end of class ControllerMain




/* end of ControllerMain.java */

