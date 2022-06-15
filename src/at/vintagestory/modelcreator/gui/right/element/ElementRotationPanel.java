package at.vintagestory.modelcreator.gui.right.element;

import java.awt.*;
import javax.swing.*;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.Start;
import at.vintagestory.modelcreator.gui.LabeledSliderComponent;
import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.interfaces.IValueUpdater;
import at.vintagestory.modelcreator.model.Element;
import at.vintagestory.modelcreator.util.QUtil;
import at.vintagestory.modelcreator.util.QUtil.QuaternionAxis;
import at.vintagestory.modelcreator.util.SpringUtilities;
import org.lwjgl.util.vector.Quaternion;

public class ElementRotationPanel extends JPanel implements IValueUpdater
{
	private final IElementManager manager;

	LabeledSliderComponent x;
	LabeledSliderComponent y;
	LabeledSliderComponent z;

	LabeledSliderComponent qx;
	LabeledSliderComponent qy;
	LabeledSliderComponent qz;
	LabeledSliderComponent qw;

	Quaternion quaternion;

	QuaternionAxis lastQuaternionChanged = QuaternionAxis.None;
	QuaternionAxis lastValidQuaternionChanged = QuaternionAxis.None;
	boolean lastQuaternionWasNegative = false;
	boolean lastValidQuaternionWasNegative = false;

	public ElementRotationPanel(IElementManager manager)
	{
		this.manager = manager;
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
		this.qx.onValueChanged(v -> this.rotQ(v, QuaternionAxis.X));
		this.qy.onValueChanged(v -> this.rotQ(v, QuaternionAxis.Y));
		this.qz.onValueChanged(v -> this.rotQ(v, QuaternionAxis.Z));
		this.qw.onValueChanged(v -> this.rotQ(v, QuaternionAxis.W));

		layout.putConstraint(SpringLayout.EAST, slidersPanel, 5, SpringLayout.EAST, z);
		layout.putConstraint(SpringLayout.SOUTH, slidersPanel, 5, SpringLayout.SOUTH, z);


		SpringUtilities.makeCompactGrid(slidersPanel, 7, 1, 0, 0, 0, 5);

		add(slidersPanel);
	}

	private void rotQ(double value, QuaternionAxis axis) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			if (lastQuaternionChanged != axis) {
				lastValidQuaternionChanged = lastQuaternionChanged;
				lastValidQuaternionWasNegative = lastQuaternionWasNegative;
			}
			lastQuaternionChanged = axis;
			lastQuaternionWasNegative = value < 0;
			System.out.println("---");
			System.out.println(quaternion);
			QUtil.setQuaternionComponent(quaternion, axis, (float) value);
			System.out.println(quaternion);
			Quaternion norm = QUtil.normalizeOthers(quaternion, axis, lastValidQuaternionChanged, lastValidQuaternionWasNegative);
			quaternion.set(norm);
			System.out.println(quaternion);
			updateQuat(cube);
			updateQuatSliders();
			updateEulerSliders(cube);
		}
	}

	private void rotX(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationX(value);
			quaternion = QUtil.ToQuaternion(
					Math.toRadians(cube.getRotationZ()),
					Math.toRadians(cube.getRotationY()),
					Math.toRadians(cube.getRotationX())
			);
			updateQuatSliders();
		}
	}

	private void rotY(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationY(value);
			quaternion = QUtil.ToQuaternion(
					Math.toRadians(cube.getRotationZ()),
					Math.toRadians(cube.getRotationY()),
					Math.toRadians(cube.getRotationX())
			);
			updateQuatSliders();
		}
	}

	private void rotZ(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationZ(value);
			quaternion = QUtil.ToQuaternion(
					Math.toRadians(cube.getRotationZ()),
					Math.toRadians(cube.getRotationY()),
					Math.toRadians(cube.getRotationX())
			);
			updateQuatSliders();
		}
	}

	// quat slider values to cube rotation
	private void updateQuat(Element cube) {
		double[] euler = QUtil.ToEulerAngles2(quaternion);
		cube.setRotationX(Math.toDegrees(euler[2]));
		cube.setRotationY(Math.toDegrees(euler[1]));
		cube.setRotationZ(Math.toDegrees(euler[0]));
	}

	// cube rotation to quat sliders
	private void updateQuatSliders() {
		this.qx.setValue(quaternion.getX());
		this.qy.setValue(quaternion.getY());
		this.qz.setValue(quaternion.getZ());
		this.qw.setValue(quaternion.getW());
	}

	// cube rotation to xyz sliders
	private void updateEulerSliders(Element cube) {
		this.x.setValue(cube.getRotationX());
		this.y.setValue(cube.getRotationY());
		this.z.setValue(cube.getRotationZ());
	}

	@Override
	public void updateValues(JComponent byGuiElem)
	{
		System.out.println("update Values Called");
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			this.x.setEnabled(true);
			this.y.setEnabled(true);
			this.z.setEnabled(true);

			this.qx.setEnabled(true);
			this.qy.setEnabled(true);
			this.qz.setEnabled(true);
			this.qw.setEnabled(true);

			updateQuatSliders();
			updateEulerSliders(cube);
		}
		else {
			this.x.setEnabled(false);
			this.y.setEnabled(false);
			this.z.setEnabled(false);

			this.qx.setEnabled(false);
			this.qy.setEnabled(false);
			this.qz.setEnabled(false);
			this.qw.setEnabled(false);

			lastQuaternionChanged = QuaternionAxis.None;
			lastValidQuaternionChanged = QuaternionAxis.None;
		}

		this.x.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.y.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.z.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
	}
}
