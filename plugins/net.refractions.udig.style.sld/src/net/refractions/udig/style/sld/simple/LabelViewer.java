/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.style.sld.simple;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.preferences.PreferenceConstants;
import net.refractions.udig.style.sld.AbstractSimpleConfigurator;
import net.refractions.udig.style.sld.SLD;
import net.refractions.udig.style.sld.internal.FontEditor;
import net.refractions.udig.style.sld.internal.Messages;
import net.refractions.udig.ui.graphics.SLDs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.expression.Expression;

/**
 * Allows editing/viewing of a Style Layer Descriptor "TextSymbolizer".
 * <p>
 * Here is the pretty picture: <pre><code>
 *          +-+ +------------+ +------+ +------+
 *   Label: |x| |     title\/| | Font | |Offset|
 *          +-+ +------------+ +------+ +------+
 * or
 *          +-+ +------------+ +------+ +------+ +------+ +--------+
 *   Label: |x| |     title\/| | Font | |VAlign| |HAlign| |Rotation|
 *          +-+ +------------+ +------+ +------+ +------+ +--------+
 * </code></pre>
 * </p>
 * <p>
 * Workflow:
 * <ol>
 * <li>createControl( parent ) - set up controls
 * <li>set( SimpleFeatureType, TextSymbolizer, Mode ) - provide content from SimpleStyleConfigurator
 *    <ol>
 *    <li> Symbolizer values copied into fields based on mode
 *    <li> fields copied into controls
 *    <li> controls enabled based on mode & fields
 *    </ol>
 * <li>Listener.widgetSelected/modifyText - User performs an "edit"
 * <li>Listener.sync( SelectionEvent ) - update fields with values of controls
 * <li>fire( SelectionSevent ) - notify SimpleStyleConfigurator of change
 * <li>get( StyleBuilder ) - construct based on fields
 * </ul>
 * </p>  
 * @author Jody Garnett
 * @since 1.0.0
 */
public class LabelViewer {
    boolean enabled;
    SimpleFeatureType schema;
    String labelType;
    FontData[] font;
    Color colour;
    
    /**
     * Use PointPlacement or LinePlacement?
     */
    boolean pointPlacement = true;
    LabelPlacement labelPlacement = null;
    
    Button on;
    Combo field;
    FontEditor fonter;
    Composite part;
    KeyListener klisten;
    
    //generic combos for PointPlacement/LinePlacement use
    Combo place;
    Combo place2;
    Combo place3;
    
    private class Listener implements SelectionListener,ModifyListener {
        public void widgetSelected( SelectionEvent e ) {
            sync(e);
        };
        public void widgetDefaultSelected( SelectionEvent e ) {
            sync(e);
        };
        public void modifyText( final ModifyEvent e ) {
            sync(AbstractSimpleConfigurator.selectionEvent(e));
        };         
        private void sync( SelectionEvent selectionEvent ) {
            try {
                LabelViewer.this.enabled = LabelViewer.this.on.getSelection();
                LabelViewer.this.colour = LabelViewer.this.fonter.getAWTColor();
                LabelViewer.this.font = LabelViewer.this.fonter.getFontList();
                LabelViewer.this.labelType = LabelViewer.this.field.getText();
            }
            catch( Throwable t ) {
                // meh
            }
            finally {                    
                LabelViewer.this.field.setEnabled(LabelViewer.this.enabled);
                LabelViewer.this.fonter.setEnabled(LabelViewer.this.enabled);
                LabelViewer.this.place.setEnabled(LabelViewer.this.enabled);
                if (LabelViewer.this.pointPlacement) {
                    LabelViewer.this.place2.setEnabled(LabelViewer.this.enabled);
                    LabelViewer.this.place2.setVisible(true);
                    LabelViewer.this.place3.setEnabled(LabelViewer.this.enabled);
                    LabelViewer.this.place3.setVisible(true);
                } else {
                    if (LabelViewer.this.place2 != null) {
                        LabelViewer.this.place2.setVisible(false);
                    }
                }
            }
            fire( selectionEvent );
        }            
    }; 
    Listener sync = new Listener();
    
    private SelectionListener listener; 
    
    /**
     * Accepts a listener that will be notified when content changes.
     * @param listener1 
     */
    public void addListener( SelectionListener listener1 ) {
        this.listener = listener1;
    }

    /**
     * Remove listener.
     * @param listener1 
     */
    public void removeListener( SelectionListener listener1 ) {
        if (this.listener == listener1)
            this.listener = null;
    }

    /**
     * TODO summary sentence for fire ...
     * 
     * @param event
     */
    protected void fire( SelectionEvent event ) {
        if (this.listener == null)
            return;
        this.listener.widgetSelected(event);
    }
    /**
     * Constructs a TextSymbolizer from the inputs
     * @param build 
     * 
     * @return TextSymbolizer defined by this model
     */
    public TextSymbolizer get( StyleBuilder build ) {
        if (!this.enabled) {
            return null;
        }
        if (this.font == null || this.font.length == 0) {
            return null;
        }
        if (this.labelType == null || "".equals(this.labelType)) {  //$NON-NLS-1$
            return null;
        }

        String fontName = this.font[0].getName();
        boolean fontBold = (this.font[0].getStyle() == SWT.BOLD);
        boolean fontItalic = (this.font[0].getStyle() == SWT.ITALIC);
        double fontSize = this.font[0].getHeight();
        Font gtFont = build.createFont(
                fontName, fontItalic, fontBold, fontSize);
        Fill fill = build.createFill(this.colour);
        
        LabelPlacement placement;
        if (pointPlacement) {
            //PointPlacement
            double horiz;
            if (this.place.getSelectionIndex() < 3) {
                switch (this.place.getSelectionIndex()) {
                case 0:
                    horiz = SLDs.ALIGN_LEFT;
                    break;
                case 1:
                    horiz = SLDs.ALIGN_CENTER;
                    break;
                case 2:
                    horiz = SLDs.ALIGN_RIGHT;
                    break;
                    
                default:
                    horiz = SLDs.ALIGN_CENTER;
                    break;
                }
            } else { //custom value
                horiz = Double.parseDouble(this.place.getText());
            }

            double vert;
            if (this.place2.getSelectionIndex() < 3) {
                switch (this.place2.getSelectionIndex()) {
                case 0:
                    vert = SLDs.ALIGN_BOTTOM;
                    break;
                case 1:
                    vert = SLDs.ALIGN_MIDDLE;
                    break;
                case 2:
                    vert = SLDs.ALIGN_TOP;
                    break;
                    
                default:
                    vert = SLDs.ALIGN_MIDDLE;
                    break;
                }
            } else { //custom value
                vert = Double.parseDouble(this.place2.getText());
            }
            
            double rotation = Double.parseDouble(this.place3.getText());
            
            placement = build.createPointPlacement(vert, horiz, rotation);
        } else {
            //LinePlacement
            double offset = Double.parseDouble(this.place.getText());
            placement = build.createLinePlacement(offset);
        }
        this.labelPlacement = placement;
        
        Expression exp = (Expression) CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()).
                property(this.labelType);
        TextSymbolizer text = build.createTextSymbolizer(fill, new Font[] {gtFont},
                null, exp, placement, null);
        if (SLD.isLine(this.schema)) {
            text.addToOptions("group", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        text.addToOptions("spaceAround", "2"); //$NON-NLS-1$ //$NON-NLS-2$
        return text; 
    }
    
    /**
     * Start editing the provided symbolizer.
     * 
     * @param schema
     * @param sym
     * @param mode 
     */
    public void set( SimpleFeatureType schema, TextSymbolizer sym, Mode mode ) {
        listen(false);
        try {
            this.schema = schema;
            this.enabled = (mode != Mode.NONE && sym != null);

            this.font = SLDs.textFont(sym);
            if (this.font == null || this.font.length == 0) {
                this.font = new FontData[]{new FontData("Arial", 12, SWT.NORMAL)}; //$NON-NLS-1$
            }
            this.labelType = SLDs.textLabelString(sym);
            this.colour = SLDs.textFontFill(sym);
            if (this.colour == null) {
                this.colour = Color.BLACK;
            }

            this.on.setEnabled(mode != Mode.NONE);
            this.fonter.setColorValue(this.colour);
            this.fonter.setFontList(this.font);

            if (schema != null) {
                AttributeDescriptor[] attrs = schema.getAttributeDescriptors().toArray(new AttributeDescriptor[0]);
                List<String> list = new ArrayList<String>();
                for( int i = 0; i < attrs.length; i++ ) {
                    Class cls = attrs[i].getType().getBinding();
                    if (String.class.isAssignableFrom(cls)) {
                        list.add(attrs[i].getName().getLocalPart());
                    } else if (Number.class.isAssignableFrom(cls)) {
                        list.add(attrs[i].getName().getLocalPart());
                    }
                }
                this.field.removeAll();
                this.field.setItems(list.toArray(new String[0]));
                if (this.labelType == null) {
                    this.field.select(0);
                } else {
                    this.field.setText(this.labelType);
                }
            }

            this.on.setSelection(this.enabled);
            this.field.setEnabled(this.enabled);
            this.fonter.setEnabled(this.enabled);
            
            if (schema != null && (SLD.isLine(schema) == pointPlacement || this.place == null)) {
                pointPlacement = !SLD.isLine(schema);
                if (pointPlacement) {
                    initPlacementContentsPoint();
                } else {
                    initPlacementContentsLine();
                }
            }
            this.place.setEnabled(this.enabled);
            if (pointPlacement) {
                //PointPlacement
                this.place2.setEnabled(this.enabled);
                this.place3.setEnabled(this.enabled);
                if (this.labelPlacement == null || !(this.labelPlacement instanceof PointPlacement)) {
                    //defaults
                    if (mode == Mode.POINT) {
                        //don't cover the point!
                        this.place.select(2); //top
                        this.place2.select(2); //right
                    } else {
                        this.place.select(1); //middle    
                        this.place2.select(1); //center
                    }
                    this.place3.select(0); //0 degrees rotation
                } else {
                    AnchorPoint anchor = ((PointPlacement) labelPlacement).getAnchorPoint();
                    String anchorX = anchor.getAnchorPointX().toString();
                    String anchorY = anchor.getAnchorPointY().toString();
                    //use labels if 0, 0.5, or 1, otherwise use value for align
                    if (anchorX.equals(Double.toString(SLDs.ALIGN_LEFT))) {
                        this.place2.select(0);
                    } else if (anchorX.equals(Double.toString(SLDs.ALIGN_CENTER))) {
                        this.place2.select(1);
                    } else if (anchorX.equals(Double.toString(SLDs.ALIGN_RIGHT))) {
                        this.place2.select(2);
                    } else {
                        this.place2.setText(anchorX);
                    }
                    if (anchorY.equals(Double.toString(SLDs.ALIGN_BOTTOM))) {
                        this.place.select(0);
                    } else if (anchorY.equals(Double.toString(SLDs.ALIGN_MIDDLE))) {
                        this.place.select(1);
                    } else if (anchorY.equals(Double.toString(SLDs.ALIGN_TOP))) {
                        this.place.select(2);
                    } else {
                        this.place.setText(anchorY);
                    }
                    //rotation
                    this.place3.setText(((PointPlacement) labelPlacement).getRotation().toString());
                }
            } else {
                //LinePlacement
                if (this.labelPlacement == null || !(this.labelPlacement instanceof LinePlacement)) {
                    //defaults
                    this.place.setText(ProjectPlugin.getPlugin().getPreferenceStore().getString(PreferenceConstants.P_STYLE_DEFAULT_PERPENDICULAR_OFFSET));
                } else {
                    String offset = ((LinePlacement) labelPlacement).getPerpendicularOffset().toString();
                    this.place.setText(offset);
                }
            }
        } finally {
            listen(true);
        }

    }
    
    /**
     * TODO summary sentence for listen ...
     * 
     * @param listen
     */
    public void listen(boolean listen) {
        if(listen) {
            this.on.addSelectionListener(this.sync);
            this.field.addSelectionListener(this.sync);
            this.field.addModifyListener(this.sync);
            this.fonter.setListener(this.sync);
            if (this.place != null) {
                this.place.addSelectionListener(this.sync);
                this.place.addModifyListener(this.sync);
            }
            if (this.place2 != null) {
                this.place2.addSelectionListener(this.sync);
                this.place2.addModifyListener(this.sync);
            }
            if (this.place3 != null) {
                this.place3.addSelectionListener(this.sync);
                this.place3.addModifyListener(this.sync);
            }
        } else {
            this.on.removeSelectionListener(this.sync);
            this.field.removeSelectionListener(this.sync);
            this.field.removeModifyListener(this.sync);
            this.fonter.clearListener();
            if (this.place != null) {
                this.place.removeSelectionListener(this.sync);
                this.place.removeModifyListener(this.sync);
            }
            if (this.place2 != null) {
                this.place2.removeSelectionListener(this.sync);
                this.place2.removeModifyListener(this.sync);
            }
            if (this.place3 != null) {
                this.place3.removeSelectionListener(this.sync);
                this.place3.removeModifyListener(this.sync);
            }
        }
    }
    
    /**
     * TODO summary sentence for createControl ...
     * 
     * @param parent
     * @param klisten 
     * @return Generated composite
     */
    public Composite createControl(Composite parent, KeyListener klisten) {
        this.part = AbstractSimpleConfigurator.subpart( parent, Messages.SimpleStyleConfigurator_label_label );
        this.klisten = klisten;
        
        this.on = new Button( part, SWT.CHECK );
        
        this.field = new Combo(part, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.field.addKeyListener(klisten);
        if(this.schema != null) {
            List<AttributeDescriptor> types = this.schema.getAttributeDescriptors();
            List<String> typeStrings = new ArrayList<String>();
            for (AttributeDescriptor attributeDescriptor : types) {
				typeStrings.add(attributeDescriptor.getLocalName());
			}
            this.field.setItems(typeStrings.toArray(new String[0]));
        }
        this.field.setToolTipText(Messages.LabelViewer_field_tooltip); 
        
        this.fonter = new FontEditor(part);
        
        //determine which placement to use
        if (schema != null) {
            if (SLD.isLine(schema)) {
                pointPlacement = false;
            } else {
                pointPlacement = true;
            }
            if (pointPlacement) {
                //point placement (3 combos: AnchorPoint (Horiz, Vert) + Rotation)
                initPlacementContentsPoint();
            } else {
                //line placement (1 combo: Perpendicular Offset)
                initPlacementContentsLine();
            }
        }
        
        listen(true);
        
        return part;    
    }
    
    private void initPlacementContentsLine() {
        if (this.place == null) {
            this.place = new Combo(part, SWT.DROP_DOWN);
            this.place.addKeyListener(klisten);
        }
        this.place.setToolTipText(Messages.LabelViewer_offset); 
        
        String[] itemsO = new String[] {"0", "5", "10", "15", "20"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        this.place.setItems(itemsO);
        
        if (this.place2 != null) {
            this.place2.setVisible(false);
        }
        if (this.place3 != null) {
            this.place3.setVisible(false);
        }
    }

    private void initPlacementContentsPoint() {
        //TODO internatialize
        String[] itemsH = new String[] {Messages.LabelViewer_left, Messages.LabelViewer_center, Messages.LabelViewer_right};   
        String[] itemsV = new String[] {Messages.LabelViewer_bottom, Messages.LabelViewer_middle, Messages.LabelViewer_top};   
        String[] itemsR = new String[] {"0", "45", "90", "135", "180", "225", "270", "315", "360"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
        
        if (this.place == null) {
            this.place = new Combo(part, SWT.DROP_DOWN);
            this.place.addKeyListener(klisten);
        }
        this.place.setToolTipText(Messages.LabelViewer_vertAlign); 
        this.place.setItems(itemsV);

        if (this.place2 == null) {
            this.place2 = new Combo(part, SWT.DROP_DOWN);
            this.place2.addKeyListener(klisten);
        }
        this.place2.setToolTipText(Messages.LabelViewer_horizAlign); 
        this.place2.setItems(itemsH);
        this.place2.setVisible(true);
        
        if (this.place3 == null) { 
            this.place3 = new Combo(part, SWT.DROP_DOWN);
            this.place3.addKeyListener(klisten);
        }
        this.place3.setToolTipText(Messages.LabelViewer_rotation); 
        this.place3.setItems(itemsR);
        this.place3.setVisible(true);
    }

}