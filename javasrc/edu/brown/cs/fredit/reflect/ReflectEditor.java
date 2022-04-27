/********************************************************************************/
/*										*/
/*		ReflectEditor.java						*/
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



package edu.brown.cs.fredit.reflect;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.xml.IvyXml;

public class ReflectEditor implements ControllerEditor, ReflectConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ControllerMain	controller_main;
private Element 	reflection_result;
private List<ReflectData> reflection_set;
private ReflectComp	reflect_component;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public ReflectEditor(ControllerMain cm)
{
   controller_main = cm;
   reflection_result = null;
   reflection_set = new ArrayList<>();
   updatePanel();

   ReflectRunner rr = new ReflectRunner();
   rr.start();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public JComponent getPanel()
{
   if (reflect_component == null) {
      reflect_component = new ReflectComp();
      updatePanel();
    }
   return reflect_component;
}



@Override public String getName()
{
   return "Reflection";
}



/********************************************************************************/
/*										*/
/*	Editor panel								*/
/*										*/
/********************************************************************************/

private void updatePanel()
{
   if (reflection_result != null) {
      reflection_set.clear();
      Set<String> done = new HashSet<>();
      for (Element e : IvyXml.children(reflection_result,"REFLECT")) {
	 ReflectData rd = new ReflectData(e);
	 String key = rd.getKey();
	 if (done.add(key)) {
	    reflection_set.add(rd);
	  }
       }
      // might want to sort the reflection results
    }

   if (reflect_component != null) {
      reflect_component.update();
    }


}


/********************************************************************************/
/*										*/
/*	Thread to get reflection results					*/
/*										*/
/********************************************************************************/

private class ReflectRunner extends Thread {

   ReflectRunner() {
      super("ReflectionRunner");
    }

   @Override public void run() {
      controller_main.waitForAnalysis();
      Element xml = controller_main.sendFaitReply(controller_main.getSessionId(),
            "REFLECTION",null,null);
      System.err.println("FREDIT: Reflection result: " + IvyXml.convertXmlToString(xml));
      reflection_result = xml;
      updatePanel();
    }

}	// end of inner class ReflectRunner



/********************************************************************************/
/*										*/
/*	Component to hold reflection items					*/
/*										*/
/********************************************************************************/

private class ReflectComp extends SwingGridPanel {

   static final long serialVersionUID = 1;

   ReflectComp() {
    }

   void update() {
      removeAll();
      if (reflection_result == null || reflection_set == null) {
	 JLabel lbl = new JLabel("Computing reflection data ... ");
	 addGBComponent(lbl,0,0,1,1,10,1);
       }
      else {
	 int y = 0;
	 for (ReflectData rd : reflection_set) {
	    JComponent cmp = rd.createComponent();
	    addGBComponent(cmp,0,y++,1,1,10,1);
	  }
       }
    }

}	// end of inner class ReflectComp




/********************************************************************************/
/*										*/
/*	Reflection data 							*/
/*										*/
/********************************************************************************/

private static class ReflectData {

   private String from_method;
   private String from_description;
   private String from_file;
   private int reflect_level;
   private int line_number;
   private String called_method;
   private String called_return;
   private String special_return;
   private String cast_type;
   private boolean cast_abstract;
   private Set<String> impl_types;

   ReflectData(Element xml) {
      from_method = IvyXml.getAttrString(xml,"CALL");
      from_description = IvyXml.getAttrString(xml,"DESCRIPTION");
      from_file = IvyXml.getAttrString(xml,"SOURCE");
      reflect_level = IvyXml.getAttrInt(xml,"LEVEL");
      called_method = IvyXml.getAttrString(xml,"METHOD");
      called_return = IvyXml.getAttrString(xml,"RETURN");
      Element ppl = IvyXml.getChild(xml,"POINT");
      line_number = IvyXml.getAttrInt(ppl,"LINE");
      Element spl = IvyXml.getChild(xml,"SPECIAL");
      if (spl == null) special_return = null;
      else special_return = IvyXml.getAttrString(spl,"RESULTTYPE");
      cast_type = null;
      impl_types = new TreeSet<>();
      cast_abstract = false;
      Element castelt = IvyXml.getChild(xml,"CAST");
      if (castelt != null) {
	 cast_abstract = IvyXml.getAttrBool(castelt,"ABSTRACT");
	 cast_abstract |= IvyXml.getAttrBool(castelt,"INTERFACE");
	 cast_type = IvyXml.getAttrString(castelt,"TYPE");
	 for (Element childelt : IvyXml.children(castelt,"CHILD")) {
	    boolean abs = IvyXml.getAttrBool(childelt,"ABSTRACT");
	    abs |= IvyXml.getAttrBool(childelt,"INTERFACE");
	    if (abs) continue;
	    impl_types.add(IvyXml.getAttrString(childelt,"TYPE"));
	  }
       }
    }

   String getCalledMethod()			{ return called_method; }
   int getLineNumber()				{ return line_number; }
   String getFromMethod()			{ return from_method; }
   String getCalledReturn()			{ return called_return; }
   String getCastType() 			{ return cast_type; }
   boolean isCastAbstract()			{ return cast_abstract; }
   Set<String> getImplementations()		{ return impl_types; }
   String getSpecialReturn()			{ return special_return; }

   String getKey() {
      String key = called_method + "@" + from_method + " @" + from_description;
      key += "@" + line_number;
      return key;
    }

   JComponent createComponent() {
      SwingGridPanel pnl = new DataPanel(this);;
      return pnl;
    }

   String getToolTipText() {
      StringBuffer buf = new StringBuffer();
      buf.append("<html><p>");
      buf.append("Call to " + getCalledMethod());
      if (reflect_level > 0) buf.append(" (" + reflect_level + ")");
      buf.append("<p>From " + from_method + from_description);
      if (from_file != null) buf.append("<p>Source file: " + from_file);
      // this needs work
      return buf.toString();
    }

}	// end of inner class ReflectData


private static class DataPanel extends SwingGridPanel {

   private transient ReflectData reflect_data;
   private static final long serialVersionUID = 1;

   DataPanel(ReflectData rd) {
      reflect_data = rd;
      int y = 0;
      addGBComponent(new JLabel("Call to " + rd.getCalledMethod()),0,y++,0,1,10,0);
      String where = "";
      if (rd.getLineNumber() > 0) where += rd.getLineNumber() + " in ";
      where += rd.getFromMethod();
      addGBComponent(new JLabel("    @ " + where),0,y++,0,1,10,0);
      addGBComponent(new JLabel("    "),0,y,1,1,0,0); // indent
      addGBComponent(new JLabel("Return type: "),1,y,1,1,0,0);
      addGBComponent(new JLabel(rd.getCalledReturn()),2,y++,1,1,10,0);
      String rtyp = rd.getCastType();
      if (rtyp != null) {
         if (rd.isCastAbstract()) rtyp += "*";
         addGBComponent(new JLabel("Cast to type: "),1,y,1,1,0,0);
         addGBComponent(new JLabel(rtyp),2,y++,1,1,10,0);
       }
      Set<String> impls = rd.getImplementations();
      if (impls != null && impls.size() > 0) {
         for (String s : impls) {
            addGBComponent(new JLabel("Implementation: "),1,y,1,1,0,0);
            addGBComponent(new JLabel(s),2,y++,1,1,10,0);
          }
       }
      String spl = rd.getSpecialReturn();
      if (spl != null && !spl.equals(rd.getCalledReturn())) {
         addGBComponent(new JLabel("Resource type: "),1,y,1,1,0,0);
         addGBComponent(new JLabel(spl),2,y++,1,1,10,0);
       }
      setToolTipText(" panel ");
      setOpaque(false);
    }

   @Override public String getToolTipText(MouseEvent evt) {
      return reflect_data.getToolTipText();
    }

}	// end of inner class DataPanel




}	// end of class ReflectEditor




/* end of ReflectEditor.java */

