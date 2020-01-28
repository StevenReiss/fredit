/********************************************************************************/
/*                                                                              */
/*              Freditor.java                                                   */
/*                                                                              */
/*      Standalone Fait Resource Editor Main program                            */
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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.brown.cs.fredit.controller.ControllerConstants;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.perfed.PerfedEditor;
import edu.brown.cs.fredit.reflect.ReflectEditor;
import edu.brown.cs.fredit.safetyed.SafetyedEditor;
import edu.brown.cs.fredit.subtyped.SubtypedEditor;

public class Freditor implements FreditorConstants, ControllerConstants.WindowCreator
{



/********************************************************************************/
/*                                                                              */
/*      Main Program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   Freditor fe = new Freditor(args);
   fe.process();
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain  fredit_controller;
private JFrame          top_frame;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private Freditor(String [] args)
{
   fredit_controller = new ControllerMain(args,this); 
}


/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

private void process()
{
   fredit_controller.setup();
   
   fredit_controller.addEditor(new SubtypedEditor(fredit_controller));
   fredit_controller.addEditor(new SafetyedEditor(fredit_controller));
   fredit_controller.addEditor(new ReflectEditor(fredit_controller));
   fredit_controller.addEditor(new PerfedEditor(fredit_controller)); 
   
   top_frame = new JFrame("FAIT Resource Editor");
   top_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   top_frame.add(fredit_controller.getPanel());
   top_frame.pack();
   top_frame.setVisible(true);
}



/********************************************************************************/
/*                                                                              */
/*      Windowing methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public void createWindow(String ttl,JComponent contents)
{
   JDialog dlg = new JDialog(top_frame,ttl,false);
   Dimension d = contents.getPreferredSize();
   JScrollPane sp = new JScrollPane(contents);
   sp.setPreferredSize(d);
   dlg.setContentPane(new JScrollPane(contents));
   dlg.pack();
   dlg.setVisible(true);
}







}       // end of class Freditor




/* end of Freditor.java */

