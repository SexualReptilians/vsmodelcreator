package at.vintagestory.modelcreator.gui;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.util.AwtUtil;
import at.vintagestory.modelcreator.util.Parser;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.function.Consumer;

public class LabeledSliderComponent extends JPanel {

    final private DecimalFormat decimalFormat = new DecimalFormat("#0.0");

    private double value;

    final private SpringLayout layout;
    final private JPanel panel;
    final private JTextField textField;
    final private JSlider slider;
    private Consumer<Double> valueChangedCallback;

    final private String label;
    final private Color color;
    private int rangeMin;
    private int rangeMax;
    private int posDefault;
    private int tickSpacing;
    private int multiplier;

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

        // Dynamically setup labels
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(this.rangeMin, new JLabel(String.format("%d°", (this.rangeMin / this.multiplier))));
        labelTable.put((this.rangeMin + this.rangeMax) / 2, new JLabel(String.format("%d°", ((this.rangeMin + this.rangeMax) / 2 / this.multiplier))));
        labelTable.put(this.rangeMax, new JLabel(String.format("%d°", (this.rangeMax / this.multiplier))));
        this.slider.setLabelTable(labelTable);

        // TODO dynamic preferred size
        this.slider.setPreferredSize(new Dimension(160, 40));

        this.slider.addChangeListener(this::onSliderValueChanged);
        AwtUtil.addChangeListener(this.textField, this::onTextFieldValueChanged);
        this.textField.addMouseWheelListener(this::onMouseWheelTextField);

        this.panel.add(textField);
        this.panel.add(slider);

        this.layout.putConstraint(SpringLayout.WEST, this.textField, 0, SpringLayout.WEST, this.panel);
        this.layout.putConstraint(SpringLayout.NORTH, this.textField, 0, SpringLayout.NORTH, this.panel);

        this.layout.putConstraint(SpringLayout.WEST, this.slider, 0, SpringLayout.EAST, this.textField);
        this.layout.putConstraint(SpringLayout.NORTH, this.slider, 0, SpringLayout.NORTH, this.panel);

        this.layout.putConstraint(SpringLayout.EAST, this.panel, 0, SpringLayout.EAST, this.slider);
        this.layout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.slider);

        add(panel);

        setValue(this.posDefault);
    }

    private void onSliderValueChanged(ChangeEvent e) {
        if (!this.slider.getValueIsAdjusting()) return;
        // do stuff only when slider is grabbed

        double step = (double)this.slider.getMajorTickSpacing() / multiplier;
        double align = this.posDefault % step;

        double val = (double)this.slider.getValue() / multiplier;
        val = step * Math.round((val - align) / step) + align;
        this.setValue(val, true);

        this.slider.setSnapToTicks(true);
    }

    private void onTextFieldValueChanged(ChangeEvent e) {
        try {
            double val = Double.parseDouble(this.textField.getText().replace(',', '.'));
            this.setValue(val);
        } catch (NumberFormatException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void onMouseWheelTextField(MouseWheelEvent e) {
        if (!this.isEnabled()) return;

        float size = e.getWheelRotation() * ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1 ? 0.1f : 1f);
        this.setValue( this.value + size, true );
    }

    public void onValueChanged(Consumer<Double> callback) {
        this.valueChangedCallback = callback;
    }

    public void setValue(double value) {
        setValue(value, false);
    }

    public void setValue(double value, boolean forced) {
        if (value == this.value && !forced) return;
        ModelCreator.ignoreValueUpdates = true;

        this.slider.setSnapToTicks(false);
        this.slider.setValue((int)(value * multiplier));
        if (!this.textField.isFocusOwner() || forced) {
            // update textField only if not focused (unless forced)
            this.textField.setText(decimalFormat.format(value));
        }

        if (valueChangedCallback != null && value != this.value) {
            valueChangedCallback.accept(value);
        }

        this.value = value;
        ModelCreator.ignoreValueUpdates = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.slider.setEnabled(enabled);
        this.textField.setEnabled(enabled);
        if (!enabled) {
            this.setValue(this.posDefault, true);
        }
    }

    public void singlePrecisionSnapping(boolean enabled) {
        if (enabled) {
            this.slider.setMajorTickSpacing(multiplier);
            this.slider.setPaintTicks(false);
        }
        else {
            this.slider.setMajorTickSpacing(this.tickSpacing);
            this.slider.setPaintTicks(true);
        }
    }
}
