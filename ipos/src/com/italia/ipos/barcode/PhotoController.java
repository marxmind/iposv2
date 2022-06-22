package com.italia.ipos.barcode;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;

import org.primefaces.event.CaptureEvent;

@Named
@ViewScoped
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class PhotoController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 575688987868681L;
	
	private String resultText;


    public void oncapture(CaptureEvent captureEvent) {
    	System.out.println("capturing.....");
    	
        try {
            if (captureEvent != null) {
                Result result = null;
                BufferedImage image = null;

                InputStream in = new ByteArrayInputStream(captureEvent.getData());

                image = ImageIO.read(in);

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                result = new MultiFormatReader().decode(bitmap);
                if (result != null) {
                    setResultText(result.getText());
                }
            }
        } catch (NotFoundException ex) {
            // fall thru, it means there is no QR code in image
        	System.out.println("no qr code found");
        } catch (ReaderException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
}
