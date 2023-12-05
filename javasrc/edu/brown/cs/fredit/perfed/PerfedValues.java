/********************************************************************************/
/*                                                                              */
/*              PerfedValues.java                                               */
/*                                                                              */
/*      Values for a data element                                               */
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



package edu.brown.cs.fredit.perfed;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

class PerfedValues implements PerfedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private int num_forward;
private int num_backward;
private int num_scan;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

PerfedValues(Element xml) 
{
   num_forward = IvyXml.getAttrInt(xml,"FORWARD",0);
   num_backward = IvyXml.getAttrInt(xml,"BACKWARD",0);
   num_scan = IvyXml.getAttrInt(xml,"SCANS",0);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/


int getNumForward()			{ return num_forward; }
int getNumBackward() 	        	{ return num_backward; }
int getNumScan()			{ return num_scan; }



/********************************************************************************/
/*                                                                              */
/*      Accumulation methods                                                    */
/*                                                                              */
/********************************************************************************/

void add(PerfedValues pv)
{
   num_forward += pv.num_forward;
   num_backward += pv.num_backward;
   num_scan += pv.num_scan;
}



/********************************************************************************/
/*                                                                              */
/*      Output methodes                                                         */
/*                                                                              */
/********************************************************************************/

@Override public String toString() 
{
   return "(" + num_forward + "," + num_backward + "," + num_scan + ")";
}


}       // end of class PerfedValues




/* end of PerfedValues.java */

