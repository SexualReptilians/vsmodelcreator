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
import at.vintagestory.modelcreator.util.SpringUtilities;
import org.lwjgl.util.vector.Quaternion;

public class ElementRotationPanel extends JPanel implements IValueUpdater
{
	private IElementManager manager;

	LabeledSliderComponent x;
	LabeledSliderComponent y;
	LabeledSliderComponent z;

	LabeledSliderComponent qx;
	LabeledSliderComponent qy;
	LabeledSliderComponent qz;
	LabeledSliderComponent qw;

	Quaternion quaternion;

	public ElementRotationPanel(IElementManager manager)
	{
		this.manager = manager;
		setMaximumSize(new Dimension(186, 270));

		SpringLayout layout = new SpringLayout();
		JPanel slidersPanel = new JPanel(layout);

		slidersPanel.setBorder(BorderFactory.createTitledBorder(Start.Border, "<html>&nbsp;&nbsp;&nbsp;<b>XYZ Rotation</b></html>"));


		x = new LabeledSliderComponent("X", Color.RED, -180, 180, 0, 22.5);
		y = new LabeledSliderComponent("Y", Color.GREEN, -180, 180, 0, 22.5);
		z = new LabeledSliderComponent("Z", Color.BLUE, -180, 180, 0, 22.5);


		qx = new LabeledSliderComponent("QX", Color.RED, -1, 1, 0, 0.1);
		qy = new LabeledSliderComponent("QY", Color.GREEN, -1, 1, 0, 0.1);
		qz = new LabeledSliderComponent("QZ", Color.BLUE, -1, 1, 0, 0.1);
		qw = new LabeledSliderComponent("QW", Color.GRAY, -1, 1, 0, 0.1);

		slidersPanel.add(x);
		slidersPanel.add(y);
		slidersPanel.add(z);

		quaternion = new Quaternion();
		slidersPanel.add(qx);
		slidersPanel.add(qy);
		slidersPanel.add(qz);
		slidersPanel.add(qw);

		x.onValueChanged(this::rotX);
		y.onValueChanged(this::rotY);
		z.onValueChanged(this::rotZ);

		qx.onValueChanged(this::rotQX);
		qy.onValueChanged(this::rotQY);
		qz.onValueChanged(this::rotQZ);
		qw.onValueChanged(this::rotQW);

		layout.putConstraint(SpringLayout.EAST, slidersPanel, 5, SpringLayout.EAST, z);
		layout.putConstraint(SpringLayout.SOUTH, slidersPanel, 5, SpringLayout.SOUTH, z);


		SpringUtilities.makeCompactGrid(slidersPanel, 7, 1, 0, 0, 0, 5);

		add(slidersPanel);
	}

	private void rotQX(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {

		}
	}

	private void rotQY(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {

		}
	}

	private void rotQZ(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {

		}
	}

	private void rotQW(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {

		}
	}

	private void rotX(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationX(value);
		}
	}

	private void rotY(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationY(value);
		}
	}

	private void rotZ(double value) {
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			cube.setRotationZ(value);
		}
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

			this.x.setValue(cube.getRotationX());
			this.y.setValue(cube.getRotationY());
			this.z.setValue(cube.getRotationZ());

			quaternion = QUtil.ToQuaternion(cube.getRotationZ(), cube.getRotationY(), cube.getRotationX());
			this.qx.setValue(quaternion.getX());
			this.qy.setValue(quaternion.getY());
			this.qz.setValue(quaternion.getZ());
			this.qw.setValue(quaternion.getW());

		}
		else {
			this.x.setEnabled(false);
			this.y.setEnabled(false);
			this.z.setEnabled(false);

			this.qx.setEnabled(false);
			this.qy.setEnabled(false);
			this.qz.setEnabled(false);
			this.qw.setEnabled(false);
		}

		this.x.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.y.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.z.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
	}
}
