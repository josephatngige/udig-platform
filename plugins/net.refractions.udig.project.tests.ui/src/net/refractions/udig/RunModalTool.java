/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.ActionTool;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.project.ui.tool.ToolLifecycleListener;

import org.eclipse.jface.action.IAction;

/**
 * Tests activating a modal tool from another tool.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class RunModalTool implements ActionTool {

    public void run() {
        IAction tool = ApplicationGIS.getToolManager().getTool("net.refractions.udig.tools.Pan",  //$NON-NLS-1$
                "net.refractions.udig.tool.category.pan"); //$NON-NLS-1$
        
        tool.run();
    }

    public void addListener( ToolLifecycleListener listener ) {
    }

    public void dispose() {
    }

    public IToolContext getContext() {
        return null;
    }

    public Object getProperty( String key ) {
        return null;
    }

    public boolean isEnabled() {
        return false;
    }

    public void removeListener( ToolLifecycleListener listener ) {
    }

    public void setContext( IToolContext tools ) {
    }

    public void setEnabled( boolean enable ) {
    }

    public void setProperty( String key, Object value ) {
    }

}
