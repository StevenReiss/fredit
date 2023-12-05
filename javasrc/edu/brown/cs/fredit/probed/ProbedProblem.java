/********************************************************************************/
/*                                                                              */
/*              ProbedProblem.java                                              */
/*                                                                              */
/*      Representation of a possible flow problem                               */
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

import org.w3c.dom.Element;


@SuppressWarnings("unused")
class ProbedProblem implements ProbedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          in_class;
private String          in_method;
private String          in_signature;
private Element         problem_xml;    


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

ProbedProblem(String cls,String mthd,String sgn,Element xml)
{
   in_class = cls;
   in_method = mthd;
   in_signature = sgn;
   problem_xml = xml;
}



}       // end of class ProbedProblem




/* end of ProbedProblem.java */

