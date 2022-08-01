package at.vintagestory.modelcreator.gui;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.Start;
import at.vintagestory.modelcreator.interfaces.IValueUpdater;
import at.vintagestory.modelcreator.util.QUtil;
import at.vintagestory.modelcreator.util.SpringUtilities;
import org.lwjgl.util.vector.Quaternion;

import javax.swing.*;
import java.awt.*;

public abstract class RotationPanel extends JPanel implements IValueUpdater {
    private final LabeledSliderComponent x;
    private final LabeledSliderComponent y;
    private final LabeledSliderComponent z;
    private final LabeledSliderComponent qx;
    private final LabeledSliderComponent qy;
    private final LabeledSliderComponent qz;
    private final LabeledSliderComponent qw;

    private final Quaternion quaternion;

    private QUtil.QuaternionAxis lastQuaternionChanged = QUtil.QuaternionAxis.None;
    private QUtil.QuaternionAxis lastValidQuaternionChanged = QUtil.QuaternionAxis.None;
    private boolean lastQuaternionWasNegative = false;
    private boolean lastValidQuaternionWasNegative = false;
    JPanel slidersPanel;

    public RotationPanel() {
        setMaximumSize(new Dimension(186, 270));

        SpringLayout layout = new SpringLayout();
        slidersPanel = new JPanel(layout);

        slidersPanel.setBorder(BorderFactory.createTitledBorder(Start.Border, "<html>&nbsp;&nbsp;&nbsp;<b>XYZ Rotation</b></html>"));

        this.quaternion = new Quaternion();

        this.x = new LabeledSliderComponent("X", new Color(0xFFB3B3, false), -180, 180, 0, 22.5);
        this.y = new LabeledSliderComponent("Y", new Color(0xBBFFB3, false), -180, 180, 0, 22.5);
        this.z = new LabeledSliderComponent("Z", new Color(0xB3C1FF, false), -180, 180, 0, 22.5);
        this.qx = new LabeledSliderComponent("QX", new Color(0xFFB3B3, false), -1, 1, 0, 0.01, 100);
        this.qy = new LabeledSliderComponent("QY", new Color(0xBBFFB3, false), -1, 1, 0, 0.01, 100);
        this.qz = new LabeledSliderComponent("QZ", new Color(0xB3C1FF, false), -1, 1, 0, 0.01, 100);
        this.qw = new LabeledSliderComponent("QW", Color.GRAY, -1, 1, 0, 0.01, 100);

        slidersPanel.add(this.x);
        slidersPanel.add(this.y);
        slidersPanel.add(this.z);
        slidersPanel.add(this.qx);
        slidersPanel.add(this.qy);
        slidersPanel.add(this.qz);
        slidersPanel.add(this.qw);

        this.x.onValueChanged(this::rotX);
        this.y.onValueChanged(this::rotY);
        this.z.onValueChanged(this::rotZ);
        this.qx.onValueChanged(v -> this.rotQ(v, QUtil.QuaternionAxis.X));
        this.qy.onValueChanged(v -> this.rotQ(v, QUtil.QuaternionAxis.Y));
        this.qz.onValueChanged(v -> this.rotQ(v, QUtil.QuaternionAxis.Z));
        this.qw.onValueChanged(v -> this.rotQ(v, QUtil.QuaternionAxis.W));

        layout.putConstraint(SpringLayout.EAST, slidersPanel, 5, SpringLayout.EAST, z);
        layout.putConstraint(SpringLayout.SOUTH, slidersPanel, 5, SpringLayout.SOUTH, z);

        SpringUtilities.makeCompactGrid(slidersPanel, 7, 1, 0, 0, 0, 5);

        add(slidersPanel);
    }

    // In degrees, {x, y, z}
    protected abstract double[] getElementRotation();

    // In degrees.
    protected abstract void setElementRotation(double x, double y, double z);

    private void rotQ(double value, QUtil.QuaternionAxis axis) {
        double[] rot = getElementRotation();
        if (rot != null) {
            if (lastQuaternionChanged != axis) {
                lastValidQuaternionChanged = lastQuaternionChanged;
                lastValidQuaternionWasNegative = lastQuaternionWasNegative;
            }
            lastQuaternionChanged = axis;
            lastQuaternionWasNegative = value < 0;
            QUtil.setQuaternionComponent(quaternion, axis, (float) value);
            quaternion.set(QUtil.normalizeOthers(quaternion, axis, lastValidQuaternionChanged, lastValidQuaternionWasNegative));

            updateQuat();
            updateQuatSliders();
            updateEulerSliders();
        }
    }

    private void rotX(double value) {
        double[] rot = this.getElementRotation();
        this.setElementRotation(value, rot[1], rot[2]);
        quaternion.set(QUtil.ToQuaternion(
                Math.toRadians(rot[2]), // z
                Math.toRadians(rot[1]), // y
                Math.toRadians(value)   // x <-
        ));
        updateQuatSliders();
    }

    private void rotY(double value) {
        double[] rot = this.getElementRotation();
        this.setElementRotation(rot[0], value, rot[2]);
        quaternion.set(QUtil.ToQuaternion(
                Math.toRadians(rot[2]), // z
                Math.toRadians(value),  // y <-
                Math.toRadians(rot[0])  // x
        ));
        updateQuatSliders();
    }

    private void rotZ(double value) {
        double[] rot = this.getElementRotation();
        this.setElementRotation(rot[0], rot[1], value);
        quaternion.set(QUtil.ToQuaternion(
                Math.toRadians(value),  // z <-
                Math.toRadians(rot[1]), // y
                Math.toRadians(rot[0])  // x
        ));
        updateQuatSliders();
    }

    // quat slider values to cube rotation
    private void updateQuat() {
        double[] euler = QUtil.ToEulerAngles2(quaternion);
        this.setElementRotation(
                Math.toDegrees(euler[2]),   // x
                Math.toDegrees(euler[1]),   // y
                Math.toDegrees(euler[0])    // z
        );
    }

    // rotation to quat sliders
    private void updateQuatSliders() {
        this.qx.setValue(quaternion.getX());
        this.qy.setValue(quaternion.getY());
        this.qz.setValue(quaternion.getZ());
        this.qw.setValue(quaternion.getW());
    }

    // rotation to xyz sliders
    private void updateEulerSliders() {
        double[] rot = getElementRotation();
        this.x.setValue(rot[0]);
        this.y.setValue(rot[1]);
        this.z.setValue(rot[2]);
    }

    @Override
    public void updateValues(JComponent byGuiElem) {
        double[] elem = this.getElementRotation();
        if (elem != null) {
            this.x.setEnabled(true);
            this.y.setEnabled(true);
            this.z.setEnabled(true);

            this.qx.setEnabled(true);
            this.qy.setEnabled(true);
            this.qz.setEnabled(true);
            this.qw.setEnabled(true);

            updateQuatSliders();
            updateEulerSliders();
        } else {
            this.x.setEnabled(false);
            this.y.setEnabled(false);
            this.z.setEnabled(false);

            this.qx.setEnabled(false);
            this.qy.setEnabled(false);
            this.qz.setEnabled(false);
            this.qw.setEnabled(false);

            lastQuaternionChanged = QUtil.QuaternionAxis.None;
            lastValidQuaternionChanged = QUtil.QuaternionAxis.None;
        }

        this.x.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
        this.y.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
        this.z.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
    }
}
