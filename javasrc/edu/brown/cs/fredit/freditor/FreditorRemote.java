/********************************************************************************/
/*                                                                              */
/*              FreditorRemote.java                                             */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.fredit.freditor;

import javax.swing.JComponent;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.perfed.PerfedEditor;
import edu.brown.cs.fredit.reflect.ReflectEditor;
import edu.brown.cs.fredit.safetyed.SafetyedEditor;
import edu.brown.cs.fredit.subtyped.SubtypedEditor;

public class FreditorRemote implements FreditorConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain fredit_controller;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public FreditorRemote(WindowCreator wc,String mint,String sid)
{
   String [] args = new String [] { "-mint", mint, "-session", sid, "-result", sid };
   fredit_controller = new ControllerMain(args,wc);
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

public JComponent getEditor()
{
   fredit_controller.setup();
   
   fredit_controller.addEditor(new SubtypedEditor(fredit_controller),true);   fredit_controller.addEditor(new SafetyedEditor(fredit_controller),true);
   fredit_controller.addEditor(new ReflectEditor(fredit_controller),true);
   fredit_controller.addEditor(new PerfedEditor(fredit_controller),false); 
   
   return fredit_controller.getPanel();
}


public void noteFaitAnalysis(Element xml) 
{
   fredit_controller.recordFaitAnalysis(xml);
}

}       // end of class FreditorRemote




/* end of FreditorRemote.java */

