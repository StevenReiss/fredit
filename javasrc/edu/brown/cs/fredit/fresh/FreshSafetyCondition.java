/********************************************************************************/
/*                                                                              */
/*              FreshSafetyCondition.java                                       */
/*                                                                              */
/*      Representation of an editable safety condition for FAIT                 */
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



package edu.brown.cs.fredit.fresh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSafetyCondition implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String condition_name;
private Map<String,FreshSafetyState> state_set;
private Set<String> event_names;
private List<FreshSafetyTransition> all_transitions;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSafetyCondition(Element xml) 
{
   condition_name = IvyXml.getAttrString(xml,"NAME");
   
   event_names = new HashSet<>();
   
   state_set = new HashMap<>();
   for (Element statexml : IvyXml.children(xml,"STATE")) {
      FreshSafetyState s = new FreshSafetyState(statexml);
      state_set.put(s.getName(),s);
    }
   
   all_transitions = new ArrayList<>();
   for (Element statexml : IvyXml.children(xml,"STATE")) {
      String nm = IvyXml.getAttrString(statexml,"NAME");
      FreshSafetyState from = state_set.get(nm);
      for (Element onxml : IvyXml.children(statexml,"ON")) {
         FreshSafetyTransition t = new FreshSafetyTransition(this,from,onxml);
         all_transitions.add(t);
         Element elsexml = IvyXml.getChild(statexml,"ELSE");
         if (elsexml != null) {
            String next = IvyXml.getAttrString(elsexml,"GOTO");
            if (next != null && !next.equals("*")) {
               FreshSafetyState to = state_set.get(next);
               FreshSafetyTransition dflt = new FreshSafetyTransition(from,to);
               all_transitions.add(dflt);
             }
          }
       }
    }
   
   fixDefaultStateTransitions();
}



FreshSafetyCondition(String nm)
{
   condition_name = nm;
   event_names = new HashSet<>();
   state_set = new HashMap<>();
   all_transitions = new ArrayList<>();
   
   FreshSafetyState initial = new FreshSafetyState("INITIAL",true);
   state_set.put(initial.getName(),initial);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

FreshSafetyState getState(String name)
{
   return state_set.get(name);
}



Collection<FreshSafetyTransition> getTransitions()
{
   return all_transitions;
}


Collection<FreshSafetyState> getStates()
{
   return state_set.values();
}


Collection<String> getEvents()
{
   return event_names;
}


/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

void defineEvent(String evt)
{
   event_names.add(evt);
}


private void fixDefaultStateTransitions()
{
   // find default transitions where EVENT always goes to STATE w/o error
   for (String evt : event_names) {
      List<FreshSafetyTransition> remtrans = new ArrayList<>();
      FreshSafetyState target = null;
      for (FreshSafetyState state : state_set.values()) {
         FreshSafetyTransition usetrans = null;
         for (FreshSafetyTransition trans : all_transitions) {
            if (trans.getFromState() == null || trans.getFromState() == state) {
               if (trans.getEvent() == null || trans.getEvent().equals(evt)) {
                  usetrans = trans;
                  break;
                }
             }
          }
         FreshSafetyState to = state;
         if (usetrans != null) to = usetrans.getToState();
         if (target == null) target = to;
         if (target != to || (usetrans != null && usetrans.hasMessage())) {
            remtrans = null;
            break;
          }
         else if (usetrans != null && usetrans.getFromState() != null &&
               usetrans.getEvent() != null) {
            remtrans.add(usetrans);
          }
       }
      if (remtrans != null) {
         FreshSafetyTransition newtrans = new FreshSafetyTransition(target,evt);
         all_transitions.add(newtrans);
         all_transitions.removeAll(remtrans);
       }
    }
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getName()                         { return condition_name; }


@Override public String toString()              // for list output
{
   return getName();
}



/********************************************************************************/
/*                                                                              */
/*      Output Methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw)
{
   xw.begin("SAFETY");
   xw.field("NAME",condition_name);
   for (FreshSafetyState state : state_set.values()) {
      state.outputXml(all_transitions,xw);
    }
   
   xw.end("SAFETY");
}

}       // end of class FreshSafetyCondition




/* end of FreshSafetyCondition.java */

