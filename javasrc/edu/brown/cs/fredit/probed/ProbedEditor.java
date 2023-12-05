/********************************************************************************/
/*                                                                              */
/*              ProbedEditor.java                                               */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.fredit.probed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.xml.IvyXml;

public class ProbedEditor implements ControllerEditor, ProbedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain          controller_main;
private JComponent              problem_component;
private List<ProbedProblem>     all_problems;
private List<ProbedProblem>     user_problems;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public ProbedEditor(ControllerMain cm)
{
   controller_main = cm;
   problem_component = null;
   
   all_problems = new ArrayList<>();
   user_problems = new ArrayList<>();
   
   loadProblems();
   
   analyzeProblems();
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()
{
   return "Flow Problems";
}


@Override public JComponent getPanel()
{
   if (problem_component == null) {
      problem_component = new ProbedPanel();
    }
   
   return problem_component;
}



/********************************************************************************/
/*                                                                              */
/*      Load problems                                                           */
/*                                                                              */
/********************************************************************************/

private void loadProblems()
{
   Element anal = controller_main.waitForAnalysis();
   
   all_problems = new ArrayList<>();
   
   Set<String> done = new HashSet<>();
   
   for (Element pxml : IvyXml.children(anal,"CALL")) {
      if (!IvyXml.getAttrBool(pxml,"INPROJECT")) continue;
      String cls = IvyXml.getAttrString(pxml,"CLASS");
      String mthd = IvyXml.getAttrString(pxml,"METHOD");
      String sgn = IvyXml.getAttrString(pxml,"SIGNATURE");
      
      for (Element exml : IvyXml.children(pxml,"ERROR")) {
         Element loc = IvyXml.getChild(exml,"POINT");
         String kind = IvyXml.getAttrString(loc,"KIND");
         
         String key = cls + "." + mthd + sgn;
         key += "@" + IvyXml.getAttrInt(loc,"LINE");
         key += "@" + IvyXml.getText(loc);
         if (!done.add(key)) continue;
         
         ProbedProblem pp = new ProbedProblem(cls,mthd,sgn,exml);
        
         if (kind != null && kind.equals("EDIT")) user_problems.add(pp);
         
         all_problems.add(pp);
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Analyze problems to find dependencies                                   */
/*                                                                              */
/********************************************************************************/

private void analyzeProblems()
{
   
}

/********************************************************************************/
/*                                                                              */
/*      Problem display panel                                                   */
/*                                                                              */
/********************************************************************************/

private class ProbedPanel extends SwingGridPanel {
   
   private static final long serialVersionUID = 1;
   
   ProbedPanel() {
      setOpaque(false);
      addGBComponent(new JLabel("Problem items go here"),0,0,-1,-1,10,10);
    }
   
}       // end of inner class ProbedPanel



}       // end of class ProbedEditor




/* end of ProbedEditor.java */

