/**
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */

package jsattrak.utilities;

/*
 * ListTransferHandler.java is used by the 1.4
 * ExtendedDnDDemo.java example.
 */
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import jsattrak.gui.JSatTrak;
import jsattrak.objects.SatelliteTleSGP4;

public class ListTransferHandler extends StringTransferHandler  implements java.io.Serializable
{
    private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
    
    // hashtable reference -- used to keep track of all sats
    Hashtable<String,SatelliteTleSGP4> satHash;
    
    // parent App
    JSatTrak parentApp;
    
    // constructor
    public ListTransferHandler(Hashtable<String,SatelliteTleSGP4> satHash, JSatTrak app)
    {
        this.satHash = satHash;
        parentApp = app;
    }
            
    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(JComponent c) 
    {
        //System.out.println("HERE");
        
        JList list = (JList)c;
        indices = list.getSelectedIndices();
        Object[] values = list.getSelectedValues();
        
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < values.length; i++) 
        {
            Object val = values[i];
            buff.append(val == null ? "" : val.toString());
            if (i != values.length - 1) {
                buff.append("\n");
            }
        }
        
        return buff.toString();
    }

    //Take the incoming string and wherever there is a
    //newline, break it into a separate item in the list.
    protected void importString(JComponent c, String str) 
    {   
        JList target = (JList)c;
        
        DefaultListModel listModel = (DefaultListModel)target.getModel();
        //System.out.println("HERE4");
        int index = target.getSelectedIndex();
        
        //System.out.println("Index: " + index);
        //System.out.println("HERE");

        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //So this is not allowed.
        if (indices != null && index >= indices[0] - 1 &&
              index <= indices[indices.length - 1]) 
        {
            indices = null;
            return;
        }

        int max = listModel.getSize();
        if (index < 0) 
        {
            index = max;
        } else 
        {
            index++;
            if (index > max) 
            {
                index = max;
            }
        }
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        int satImportedCount = 0;
        for (int i = 0; i < values.length; i++) 
        {
            // now we need to split again using ### to seperate names from TLE lines
            
            String[] tleLines = values[i].split("###");
            
            String type = tleLines[0]; // ASSUMED HERE TO BE SAT (should check this now that more objects added)
            String name = tleLines[1];
            String tleLine1 = tleLines[2];
            String tleLine2 = tleLines[3];
            
            // see if sat already exisits, if not add to hashtable and add to list
            if( !satHash.containsKey(name) )
            {
                // is not already in list
                // add to hashTable
                try
                {
                    SatelliteTleSGP4 prop = new SatelliteTleSGP4(name, tleLine1, tleLine2);

                    satHash.put(name, prop);

                    // propogate satellite to current date
                    prop.propogate2JulDate(parentApp.getCurrentJulTime(),true);

                    listModel.add(index++, name); // add to the list

                    // count number of imports
                    satImportedCount++;
                }
                catch (Exception e)
                {
                }
                
            }
            
        } // for each sat dragged
        
        // update GUI if needed
        if(satImportedCount > 0)
        {
            parentApp.forceRepainting();
        }
        
    } // importString

    //If the remove argument is true, the drop has been
    //successful and it's time to remove the selected items 
    //from the list. If the remove argument is false, it
    //was a Copy operation and the original list is left
    //intact.
    protected void cleanup(JComponent c, boolean remove) 
    {
        // do not remove it! -- (uncomment to be able to drag out and remove from list)
        
//        if (remove && indices != null)
//        {
//            JList source = (JList)c;
//            DefaultListModel model  = (DefaultListModel)source.getModel();
//            //If we are moving items around in the same list, we
//            //need to adjust the indices accordingly, since those
//            //after the insertion point have moved.
//            if (addCount > 0)
//            {
//                for (int i = 0; i < indices.length; i++)
//                {
//                    if (indices[i] > addIndex)
//                    {
//                        indices[i] += addCount;
//                    }
//                }
//            }
//            for (int i = indices.length - 1; i >= 0; i--)
//            {
//                model.remove(indices[i]);
//            }
//        }
//        indices = null;
//        addCount = 0;
//        addIndex = -1;
    }
}
