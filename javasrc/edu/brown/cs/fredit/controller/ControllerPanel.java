/********************************************************************************/
/*                                                                              */
/*              ControllerPanel.java                                            */
/*                                                                              */
/*      Main panel for FAIT resource editor                                     */
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



package edu.brown.cs.fredit.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.brown.cs.ivy.swing.SwingGridPanel;

class ControllerPanel implements ControllerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain  fredit_main;
private MainPanel       main_panel;

private static final int        TAB_WIDTH = 500;
private static final int        TAB_HEIGHT = 400;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

ControllerPanel(ControllerMain cm)
{
   fredit_main = cm;
   main_panel = null;
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

JPanel getPanel()
{
   if (main_panel == null) main_panel = new MainPanel();
   
   return main_panel;
}


void addTab(String name,JComponent tab,boolean scroll)
{
   if (main_panel == null) main_panel = new MainPanel();
   
   if (scroll) main_panel.addTab(name,new JScrollPane(tab));
   else main_panel.addTab(name,tab);
}



Component getCurrentTab()
{
   return main_panel.getCurrentTab();
}



/********************************************************************************/
/*                                                                              */
/*      Main Panel subclass                                                     */
/*                                                                              */
/********************************************************************************/

private class MainPanel extends SwingGridPanel implements ActionListener {
   
   private JTabbedPane tab_pane;
   private static final long serialVersionUID = 1;   
   
   MainPanel() { 
      Color bc = new Color(0xf0a27d);
      setBackground(bc);
      
      beginLayout();
      addBannerLabel("FAIT Resource Editor for " + fredit_main.getWorkspaceName());
      addSeparator();
      tab_pane = new JTabbedPane(JTabbedPane.TOP);
      tab_pane.setPreferredSize(new Dimension(TAB_WIDTH,TAB_HEIGHT));
      tab_pane.setBackground(bc);
      tab_pane.setOpaque(false);
      addLabellessRawComponent("TABPANE",tab_pane,true,true);
      addSeparator();
      addBottomButton("Revert","REVERT",this);
      addBottomButton("Save","SAVE",this);
      addBottomButton("Done","DONE",this);
      addBottomButtons();
      
      setOpaque(true);
    }
   
   void addTab(String name,JComponent tab) {
      tab_pane.addTab(name,tab);
    }
   
   Component getCurrentTab() {
      return tab_pane.getSelectedComponent();
    }
   
   @Override protected void paintComponent(Graphics g) {
      // Color bc = new Color(0xf0a27d);
      // Dimension sz = getSize();
      // g.setColor(bc);
      // g.fillRect(0,0,sz.width,sz.height);
     
      super.paintComponent(g);
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String cmd = evt.getActionCommand();
      if (cmd.equalsIgnoreCase("REVERT")) {
         // handle revert
       }
      else if (cmd.equalsIgnoreCase("SAVE")) {
         // handle save
         close();
       }
      else if (cmd.equalsIgnoreCase("DONE")) {
         close();
       }
    }
   
   private void close() {
      ControllerMain.getDisplayParent(this).setVisible(false);
      if (!fredit_main.usingBubbles()) System.exit(0);
    }
   
   
}       // end of inner class MainPanel



}       // end of class ControllerPanel




/* end of ControllerPanel.java */

