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
    protected final LabeledSliderComponent x;
    protected final LabeledSliderComponent y;
    protected final LabeledSliderComponent z;
    protected final LabeledSliderComponent qx;
    protected final LabeledSliderComponent qy;
    protected final LabeledSliderComponent qz;
    protected final LabeledSliderComponent qw;

    protected final Quaternion quaternion;

    protected QUtil.QuaternionAxis lastQuaternionChanged = QUtil.QuaternionAxis.None;
    protected QUtil.QuaternionAxis lastValidQuaternionChanged = QUtil.QuaternionAxis.None;
    private boolean lastQuaternionWasNegative = false;
    private boolean lastValidQuaternionWasNegative = false;

    public RotationPanel() {
        setMaximumSize(new Dimension(186, 270));

        SpringLayout layout = new SpringLayout();
        JPanel slidersPanel = new JPanel(layout);

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
        double[] rot = this.getElementRotation();
        if (rot == null) return;

        if (lastQuaternionChanged != axis) {
            lastValidQuaternionChanged = lastQuaternionChanged;
            lastValidQuaternionWasNegative = lastQuaternionWasNegative;
        }
        lastQuaternionChanged = axis;
        lastQuaternionWasNegative = value < 0;
        QUtil.setQuaternionComponent(quaternion, axis, (float) value);
        quaternion.set(QUtil.normalizeOthers(quaternion, axis, lastValidQuaternionChanged, lastValidQuaternionWasNegative));

        updateQuat();
        updateEulerSliders();
    }

    private void rotX(double value) {
        double[] rot = this.getElementRotation();
        if (rot == null) return;

        this.setElementRotation(value, rot[1], rot[2]);
        setQuatFromEuler(rot[2], rot[1], value);
        updateQuatSliders();
    }

    private void rotY(double value) {
        double[] rot = this.getElementRotation();
        if (rot == null) return;

        this.setElementRotation(rot[0], value, rot[2]);
        setQuatFromEuler(rot[2], value, rot[0]);
        updateQuatSliders();
    }

    private void rotZ(double value) {
        double[] rot = this.getElementRotation();
        if (rot == null) return;

        this.setElementRotation(rot[0], rot[1], value);
        setQuatFromEuler(value, rot[1], rot[0]);
        updateQuatSliders();
    }

    // store quaternion from zyx
    private void setQuatFromEuler(double yaw, double pitch, double roll) {
        quaternion.set(QUtil.ToQuaternion(
                Math.toRadians(yaw),    // z
                Math.toRadians(pitch),  // y
                Math.toRadians(roll)    // x
        ));
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
    protected void updateQuatSliders() {
        this.qx.setValue(quaternion.getX());
        this.qy.setValue(quaternion.getY());
        this.qz.setValue(quaternion.getZ());
        this.qw.setValue(quaternion.getW());
    }

    // rotation to xyz sliders
    protected void updateEulerSliders() {
        double[] rot = this.getElementRotation();
        if (rot == null) return;

        this.x.setValue(rot[0]);
        this.y.setValue(rot[1]);
        this.z.setValue(rot[2]);
        updateQuatSliders();
    }

    @Override
    public void updateValues(JComponent byGuiElem) {
        double[] rot = this.getElementRotation();
        if (rot != null) {
            this.x.setEnabled(true);
            this.y.setEnabled(true);
            this.z.setEnabled(true);

            this.qx.setEnabled(true);
            this.qy.setEnabled(true);
            this.qz.setEnabled(true);
            this.qw.setEnabled(true);

            setQuatFromEuler(rot[2], rot[1], rot[0]);
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
