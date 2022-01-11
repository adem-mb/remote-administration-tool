package application.WindowCtrl;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import application.Utils.Functions;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;

public class Recorder {
	@FXML
	ImageView imgViwer;
	public int sn;
	private Image img;
	BufferedImage sim;

	public void setDataM(byte[] data) {
		long startTime1 = System.currentTimeMillis();
		String da = Functions.Byte2Str(data);
		application.MainWindowController.server.send(sn, "RecSrt" + application.MainWindowController.server.SPL);
		if (!da.equals("noch")) {
			try {
				String spldata[] = da.split(Pattern.quote("*||*"));
				Graphics gr = sim.getGraphics();
				for (String subd : spldata) {
					String constr[] = subd.split((Pattern.quote("/*-")));
					int x = Integer.parseInt(constr[0]);
					int y = Integer.parseInt(constr[1]);
					byte byteImage[] = Functions.Str2Byte(constr[2]);
					// System.out.println(constr.);

					ByteArrayInputStream ar = new ByteArrayInputStream(byteImage);
					BufferedImage Buf = ImageIO.read(ar);
					gr.drawImage(Buf, x, y, null);

				}
			} catch (NullPointerException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// ByteArrayInputStream ar=new ByteArrayInputStream(data);
			Image image = SwingFXUtils.toFXImage(sim, null);
			// img=new Image(ar);

			imgViwer.setImage(image);

			// imgViwer.setImage(new Image(new
			// ByteArrayInputStream(Base64.getDecoder().decode(data))));
			long endTime1 = System.currentTimeMillis();
			long duration1 = (endTime1 - startTime1);
			System.out.println((duration1));
			img = null;

		}

		System.gc();
	}

	public void setData(byte[] data) {

		ByteArrayInputStream ar = new ByteArrayInputStream(data);
		try {
			sim = ImageIO.read(ar);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		img = SwingFXUtils.toFXImage(sim, null);

		imgViwer.setImage(img);
		// imgViwer.setImage(new Image(new
		// ByteArrayInputStream(Base64.getDecoder().decode(data))));
		application.MainWindowController.server.send(sn, "RecSrt" + application.MainWindowController.server.SPL);
		try {
			ar.close();
			img = null;
			System.gc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void mouseUp(MouseEvent e) {

		double x = e.getX();
		double y = e.getY();

		ImageView view = (ImageView) e.getSource();
		Bounds bounds = view.getLayoutBounds();
		double xScale = view.getImage().getWidth() / bounds.getWidth();
		double yScale = view.getImage().getHeight() / bounds.getHeight();

		x *= xScale;
		y *= yScale;

		int xCord = (int) x;
		int yCord = (int) y;
		application.MainWindowController.server.send(sn,
				"mouseup" + application.MainWindowController.server.SPL + xCord + application.MainWindowController.server.SPL + yCord);
	}
}
