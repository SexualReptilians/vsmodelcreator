package at.vintagestory.modelcreator.gui;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.util.AwtUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.function.Consumer;

public class LabeledSliderComponent extends JPanel {

    final private DecimalFormat decimalFormat = new DecimalFormat("0.0");

    final private SpringLayout layout;
    final private JPanel panel;
    final private JTextField textField;
    final private JSlider slider;

    private Consumer<Double> valueChangedCallback;

    final private String label;
    final private Color color;

    private int multiplier;

    private double value;
    private int rangeMin;
    private int rangeMax;
    private int posDefault;
    private int tickSpacing;
    private double sliderStep;
    private double sliderStepAlign;
    private boolean flashing = false;

    public LabeledSliderComponent(String label, Color color, int rangeMin, int rangeMax, int posDefault, int tickSpacing) {
        this(label, color, rangeMin, rangeMax, posDefault, tickSpacing, 1);
    }

    // Default ticks 1
    public LabeledSliderComponent(String label, Color color, int rangeMin, int rangeMax, int posDefault) {
        this(label, color, rangeMin, rangeMax, posDefault, 1, 1);
    }

    public LabeledSliderComponent(String label, Color color, int rangeMin, int rangeMax, int posDefault, double tickSpacing) {
        this(label, color, rangeMin, rangeMax, posDefault, tickSpacing, 10);

    }

    private LabeledSliderComponent(String label, Color color, int rangeMin, int rangeMax, int posDefault, double tickSpacing, int multiplier) {
        // Initial variables
        this.label = label;
        this.color = color;
        this.multiplier = multiplier;
        this.rangeMin = rangeMin * this.multiplier;
        this.rangeMax = rangeMax * this.multiplier;
        this.posDefault = posDefault * this.multiplier;
        this.tickSpacing = (int)(tickSpacing * this.multiplier);

        // Setup panel
        this.layout = new SpringLayout();
        this.panel = new JPanel(this.layout);

        // Setup text field
        this.textField = new JTextField();
        this.textField.setPreferredSize(new Dimension(42, 20));
        this.textField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        this.textField.setHorizontalAlignment(JTextField.RIGHT);
        this.textField.setForeground(Color.BLACK);
        this.textField.setBackground(this.color);

        // Setup slider
        this.slider = new JSlider(JSlider.HORIZONTAL, this.rangeMin, this.rangeMax, this.posDefault);
        this.slider.setMajorTickSpacing(this.tickSpacing);
        this.slider.setPaintTicks(true);
        this.slider.setPaintLabels(true);
        this.singlePrecisionSnapping(false);

        // Dynamically setup slider labels
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(this.rangeMin, new JLabel(String.format("%d", (this.rangeMin / this.multiplier))));
        labelTable.put((this.rangeMin + this.rangeMax) / 2, new JLabel(String.format("%d", ((this.rangeMin + this.rangeMax) / 2 / this.multiplier))));
        labelTable.put(this.rangeMax, new JLabel(String.format("%d", (this.rangeMax / this.multiplier))));
        this.slider.setLabelTable(labelTable);

        // TODO dynamic preferred size
        this.slider.setPreferredSize(new Dimension(160, 40));

        // Add event listeners
        this.slider.addChangeListener(this::onSliderValueChanged);
        AwtUtil.addChangeListener(this.textField, this::onTextFieldValueChanged);
        this.textField.addMouseWheelListener(this::onMouseWheelTextField);

        // Put elements in panel
        this.panel.add(textField);
        this.panel.add(slider);

        // Setup panel layout
        this.layout.putConstraint(SpringLayout.WEST, this.textField, 0, SpringLayout.WEST, this.panel);
        this.layout.putConstraint(SpringLayout.NORTH, this.textField, 0, SpringLayout.NORTH, this.panel);

        this.layout.putConstraint(SpringLayout.WEST, this.slider, 0, SpringLayout.EAST, this.textField);
        this.layout.putConstraint(SpringLayout.NORTH, this.slider, 0, SpringLayout.NORTH, this.panel);

        this.layout.putConstraint(SpringLayout.EAST, this.panel, 0, SpringLayout.EAST, this.slider);
        this.layout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.slider);

        super.add(panel);

        // set initial default value
        setValue(this.posDefault, true);
    }

    private void onSliderValueChanged(ChangeEvent e) {
        // do stuff only when slider is grabbed
        if (!this.slider.getValueIsAdjusting()) return;

        // If we're manipulating the slider, snap to ticks
        this.slider.setSnapToTicks(true);

        // Snap directly to ticks without intermediate values
        double val = (double)this.slider.getValue() / this.multiplier;
        val = this.sliderStep * Math.round((val - this.sliderStepAlign) / this.sliderStep) + this.sliderStepAlign;

        this.setValue(val, true);
    }

    private void onTextFieldValueChanged(ChangeEvent e) {
        // Set valid values else reject and notify
        try {
            double val = Double.parseDouble(this.textField.getText().replace(',', '.'));
            this.setValue(val);
        } catch (NumberFormatException ex) {
            flashTextField();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void onMouseWheelTextField(MouseWheelEvent e) {
        // Ignore wheel events when element disabled
        if (!this.isEnabled()) return;

        // Multiplier for holding shift
        float size = e.getWheelRotation() * ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1 ? 0.1f : 1f);
        this.setValue(this.value + size, true);
    }

    public void onValueChanged(Consumer<Double> callback) {
        this.valueChangedCallback = callback;
    }

    public void setValue(double value) {
        setValue(value, false);
    }

    public void setValue(double value, boolean forced) {
        // Dont recursively repeat events
        if (value == this.value && !forced) return;

        // stop slider tick snapping (for some reason it snaps to ticks even if set programmatically)
        this.slider.setSnapToTicks(false);
        this.slider.setValue((int)(value * multiplier));

        // update textField only if not focused (unless forced)
        if (!this.textField.isFocusOwner() || forced) {
            this.textField.setText(decimalFormat.format(value));
        }

        // Call the callback function
        if (valueChangedCallback != null && value != this.value && !ModelCreator.ignoreValueUpdates) {
            ModelCreator.ignoreValueUpdates = true;
            valueChangedCallback.accept(value);
            ModelCreator.ignoreValueUpdates = false;
        }

        this.value = value;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Enable or disable the element
        super.setEnabled(enabled);
        this.slider.setEnabled(enabled);
        this.textField.setEnabled(enabled);
        if (!enabled) {
            this.setValue(this.posDefault, true);
        }
    }

    public void singlePrecisionSnapping(boolean enabled) {
        // allow moving sliders in 1s integers
        if (enabled) {
            this.slider.setMajorTickSpacing(multiplier);
            this.slider.setPaintTicks(false);
        }
        else {
            this.slider.setMajorTickSpacing(this.tickSpacing);
            this.slider.setPaintTicks(true);
        }

        // fix steps
        this.sliderStep = (double)this.slider.getMajorTickSpacing() / this.multiplier;
        this.sliderStepAlign = this.posDefault % this.sliderStep;
    }

    private void flashTextField() {
        // dont flash if flashing
        if (this.flashing) return;
        this.flashing = true;

        // parameters for flashing
        Color originalColor = this.textField.getForeground();
        Color newColor = new Color(255 - originalColor.getRed(), 255 - originalColor.getGreen(), 255 - originalColor.getBlue());
        int flashDelay = 75;
        int flashLength = 500;
        int totalCount = flashLength / flashDelay;

        // Flash timer
        javax.swing.Timer timer = new javax.swing.Timer(flashDelay, new ActionListener(){
            int count = 0;
            public void actionPerformed(ActionEvent evt) {
                // Every odd number change color
                if (count % 2 == 0) {
                    textField.setForeground(newColor);
                } else {
                    textField.setForeground(originalColor);
                    // once done flashing stop timer
                    if (count >= totalCount) {
                        ((Timer)evt.getSource()).stop();
                        flashing = false;
                    }
                }
                count++;
            }
        });

        timer.start();
    }
}
