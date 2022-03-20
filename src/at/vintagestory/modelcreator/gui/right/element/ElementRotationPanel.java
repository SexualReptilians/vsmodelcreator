package at.vintagestory.modelcreator.gui.right.element;

import java.awt.*;
import javax.swing.*;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.Start;
import at.vintagestory.modelcreator.gui.LabeledSliderComponent;
import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.interfaces.IValueUpdater;
import at.vintagestory.modelcreator.model.Element;
import at.vintagestory.modelcreator.util.SpringUtilities;

public class ElementRotationPanel extends JPanel implements IValueUpdater
{
	private IElementManager manager;

	LabeledSliderComponent x;
	LabeledSliderComponent y;
	LabeledSliderComponent z;

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

		slidersPanel.add(x);
		slidersPanel.add(y);
		slidersPanel.add(z);

		x.onValueChanged(this::rotX);
		y.onValueChanged(this::rotY);
		z.onValueChanged(this::rotZ);

		layout.putConstraint(SpringLayout.EAST, slidersPanel, 5, SpringLayout.EAST, z);
		layout.putConstraint(SpringLayout.SOUTH, slidersPanel, 5, SpringLayout.SOUTH, z);


		SpringUtilities.makeCompactGrid(slidersPanel, 3, 1, 0, 0, 0, 5);

		add(slidersPanel);
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
		System.out.println("Test");
		Element cube = manager.getCurrentElement();
		if (cube != null) {
			this.x.setEnabled(true);
			this.y.setEnabled(true);
			this.z.setEnabled(true);

			this.x.setValue(cube.getRotationX());
			this.y.setValue(cube.getRotationY());
			this.z.setValue(cube.getRotationZ());
		}
		else {
			this.x.setEnabled(false);
			this.y.setEnabled(false);
			this.z.setEnabled(false);
		}

		this.x.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.y.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
		this.z.singlePrecisionSnapping(ModelCreator.currentProject.AllAngles);
	}
}
