package at.vintagestory.modelcreator;

import java.awt.Color;
import java.io.File;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarkLaf;
import org.lwjgl.LWJGLUtil;

public class Start
{
	public static Color BorderColor = new Color(220, 220, 220);
	public static javax.swing.border.Border Border = BorderFactory.createLineBorder(BorderColor, 0);
	
	public static void main(String[] args)
	{
		Double version = Double.parseDouble(System.getProperty("java.specification.version"));
		if (version < 1.8)
		{
			JOptionPane.showMessageDialog(null, "You need Java 1.8 or higher to run this program.");
			return;
		}

		System.setProperty("org.lwjgl.util.Debug", "true");

		File JGLLib = null;
		switch(LWJGLUtil.getPlatform())
		{
		    case LWJGLUtil.PLATFORM_WINDOWS:
		    {
		        JGLLib = new File("./natives/windows/");
		    }
		    break;

		    case LWJGLUtil.PLATFORM_LINUX:
		    {
		        JGLLib = new File("./natives/linux/");
		    }
		    break;

		    case LWJGLUtil.PLATFORM_MACOSX:
		    {
		        JGLLib = new File("./natives/macosx/");
		    }
		    break;
		}

		System.setProperty("org.lwjgl.librarypath", JGLLib.getAbsolutePath());
		
		try
		{
			FlatDarkLaf.setup();
			//UIManager.put("TabbedPane.tabWidthMode", "equal");
			UIManager.put("TabbedPane.minimumTabWidth", 80);
			UIManager.put("TabbedPane.showTabSeparators", true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e);
		}

		try {
			new ModelCreator("(untitled) - " + ModelCreator.windowTitle, args);	
		} catch(Exception e1) {
			JOptionPane.showMessageDialog(
				null, 
				"Program crashed, please make a screenshot of this message and report it, program will exit now sorry about that :(\nException: " + e1 + "\n" + ModelCreator.stackTraceToString(e1), 
				"Crash!", 
				JOptionPane.ERROR_MESSAGE, 
				null
			);
		}
		
	}
}
