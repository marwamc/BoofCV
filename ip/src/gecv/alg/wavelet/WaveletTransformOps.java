/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.wavelet;

import gecv.alg.wavelet.impl.ImplWaveletTransformBorder;
import gecv.alg.wavelet.impl.ImplWaveletTransformInner;
import gecv.alg.wavelet.impl.ImplWaveletTransformNaive;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt16;
import gecv.struct.image.ImageUInt8;
import gecv.struct.wavelet.WaveletDescription;
import gecv.struct.wavelet.WlCoef_F32;
import gecv.struct.wavelet.WlCoef_I32;


/**
 * <p>
 * Functional interface for applying general purpose wavelet and inverse wavelet transforms.
 * </p>
 *
 * <p>
 * A single level wavelet transform breaks the image up into four regions:
 * <table border="1">
 * <tr><td>a</td><td>h</td></tr>
 * <tr><td>v</td><td>d</td></tr>
 * </table>
 * Each region has M/2,N/2 rows and columns. Region 'a' is the scaling image, 'h' and 'v' are
 * a combination of scaling and wavelet, and 'd' is a combination of horizontal and vertical wavelets.
 * When a multiple level transform is performed then the input to the next level is the 'a' from the previous
 * level.
 * </p>
 *
 * @author Peter Abeles
 */
public class WaveletTransformOps {

	/**
	 * <p>
	 * Performs a single level wavelet transform.
	 * </p>
	 *
	 * @param desc Description of the wavelet.
	 * @param input Input image. Not modified.
	 * @param output Where the wavelet transform is written to. Modified.
	 * @param storage Optional storage image.  Should be the same size as output image. If null then
	 * an image is declared internally.
	 */
	public static void transform1( WaveletDescription<WlCoef_F32> desc ,
								   ImageFloat32 input , ImageFloat32 output ,
								   ImageFloat32 storage )
	{
		UtilWavelet.checkShape(input,output);

		WlCoef_F32 coef = desc.getForward();

		if( output.width < coef.scaling.length || output.width < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		if( output.height < coef.scaling.length || output.height < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		storage = checkDeclareStorage(output.width, output.height, storage,"storage1");

		// the faster routines can only be run on images which are not too small
		int minSize = Math.max(coef.getScalingLength(),coef.getWaveletLength())*3;

		if( input.getWidth() <= minSize || input.getHeight() <= minSize ) {
			ImplWaveletTransformNaive.horizontal(desc.getBorder(),coef,input,storage);
			ImplWaveletTransformNaive.vertical(desc.getBorder(),coef,storage,output);
		} else {
			ImplWaveletTransformInner.horizontal(coef,input,storage);
			ImplWaveletTransformBorder.horizontal(desc.getBorder(),coef,input,storage);
			ImplWaveletTransformInner.vertical(coef,storage,output);
			ImplWaveletTransformBorder.vertical(desc.getBorder(),coef,storage,output);
		}
	}

	/**
	 * <p>
	 * Performs a single level wavelet transform.
	 * </p>
	 *
	 * @param desc Description of the wavelet.
	 * @param input Input image. Not modified.
	 * @param output Where the wavelet transform is written to. Modified.
	 * @param storage Optional storage image.  Should be the same size as output image. If null then
	 * an image is declared internally.
	 */
	// todo add unit tests
	public static void transform1( WaveletDescription<WlCoef_I32> desc ,
								   ImageUInt8 input , ImageSInt16 output ,
								   ImageSInt16 storage )
	{
		UtilWavelet.checkShape(input,output);

		WlCoef_I32 coef = desc.getForward();

		if( output.width < coef.scaling.length || output.width < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		if( output.height < coef.scaling.length || output.height < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		storage = checkDeclareStorage(output.width, output.height, storage,"storage1");

		// the faster routines can only be run on images which are not too small
		int borderLower = UtilWavelet.borderForwardLower(desc.forward);

		int smallW = Math.max(borderLower,UtilWavelet.borderForwardUpper(desc.forward,output.width));
		int smallH = Math.max(borderLower,UtilWavelet.borderForwardUpper(desc.forward,output.height));

		if( input.getWidth() <= smallW*2 || input.getHeight() <= smallH*2 ) {
			ImplWaveletTransformNaive.horizontal(desc.getBorder(),coef,input,storage);
			ImplWaveletTransformNaive.vertical(desc.getBorder(),coef,storage,output);
		} else {
			ImplWaveletTransformInner.horizontal(coef,input,storage);
			ImplWaveletTransformBorder.horizontal(desc.getBorder(),coef,input,storage);
//			ImplWaveletTransformInner.vertical(coef,storage,output);
//			ImplWaveletTransformBorder.vertical(desc.getBorder(),coef,storage,output);
		}
	}

	/**
	 * <p>
	 * Performs a level N wavelet transform using the fast wavelet transform (FWT).
	 * </p>
	 *
	 * <p>To save memory the input image is used to store intermediate results and is modified.</p>
	 *
	 * @param desc Description of the wavelet.
	 * @param input Input image and is used as internal workspace. Modified.
	 * @param output Where the multilevel wavelet transform is written to. Modified.
	 * @param storage Optional storage image.  Should be the same size as output image. If null then
	 * an image is declared internally.
	 * @param numLevels Number of levels which should be computed in the transform.
	 */
	public static void transformN( WaveletDescription<WlCoef_F32> desc ,
								   ImageFloat32 input , ImageFloat32 output ,
								   ImageFloat32 storage ,
								   int numLevels )
	{
		if( numLevels == 1 ) {
			transform1(desc,input,output, storage);
			return;
		}

		UtilWavelet.checkShape(desc.getForward(),input,output,numLevels);
		storage = checkDeclareStorage(output.width, output.height, storage,"storage");
		// modify the shape of a temporary image not the original
		storage = storage.subimage(0,0,output.width,output.height);

		transform1(desc,input,output, storage);

		for( int i = 2; i <= numLevels; i++ ) {
			int width = output.width/2;
			int height = output.height/2;
			width += width%2;
			height += height%2;

			input = input.subimage(0,0,width,height);
			output = output.subimage(0,0,width,height);
			input.setTo(output);

			// transform the scaling image and save the results in the output image
			storage.reshape(width,height);
			transform1(desc,input,output,storage);
		}
	}
	/**
	 * <p>
	 * Performs a single level inverse wavelet transform. Do not pass in a whole image which has been
	 * transformed by a multilevel transform.  Just the relevant sub-image.
	 * </p>
	 *
	 * @param desc Description of the inverse wavelet.
	 * @param input Input wavelet transform. Not modified.
	 * @param output Reconstruction of original image. Modified.
	 * @param storage Optional storage image.  Should be the same size as the input image. If null then
	 * an image is declared internally.
	 */
	public static void inverse1( WaveletDescription<WlCoef_F32> desc ,
								 ImageFloat32 input , ImageFloat32 output ,
								 ImageFloat32 storage )
	{
		UtilWavelet.checkShape(output,input);
		WlCoef_F32 coef = desc.getForward();
		if( output.width < coef.scaling.length || output.width < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		if( output.height < coef.scaling.length || output.height < coef.wavelet.length )
			throw new IllegalArgumentException("Wavelet is too large for provided image.");
		storage = checkDeclareStorage(input.width, input.height, storage,"storage");

		// the faster routines can only be run on images which are not too small
		int minSize = Math.max(coef.getScalingLength(),coef.getWaveletLength())*3;

		if( output.getWidth() <= minSize || output.getHeight() <= minSize ) {
			ImplWaveletTransformNaive.verticalInverse(desc.getBorder(),desc.getInverse(),input,storage);
			ImplWaveletTransformNaive.horizontalInverse(desc.getBorder(),desc.getInverse(),storage,output);
		} else {
			ImplWaveletTransformInner.verticalInverse(desc.getInverse().getInnerCoefficients(),input,storage);
			ImplWaveletTransformBorder.verticalInverse(desc.getBorder(),desc.getInverse(),input,storage);
			ImplWaveletTransformInner.horizontalInverse(desc.getInverse().getInnerCoefficients(),storage,output);
			ImplWaveletTransformBorder.horizontalInverse(desc.getBorder(),desc.getInverse(),storage,output);
		}
	}

	/**
	 * <p>Performs a level N inverse fast wavelet transform (FWT).</p>
	 *
	 * <p>To save memory the input image is used to store intermediate results and is modified.</p>
	 *
	 * @param desc Description of the inverse wavelet.
	 * @param input Input wavelet transform and is used as internal workspace. Modified.
	 * @param output Reconstruction of original image. Modified.
	 * @param storage Optional storage image.  Should be the same size as the input image. If null then
	 * an image is declared internally.
	 * @param numLevels Number of levels in the transform.
	 */
	public static void inverseN( WaveletDescription<WlCoef_F32> desc ,
								 ImageFloat32 input , ImageFloat32 output ,
								 ImageFloat32 storage,
								 int numLevels )
	{
		if( numLevels == 1 ) {
			inverse1(desc,input,output, storage);
			return;
		}

		UtilWavelet.checkShape(desc.getForward(),output,input,numLevels);
		storage = checkDeclareStorage(input.width, input.height, storage,"storage");
		// modify the shape of a temporary image not the original
		storage = storage.subimage(0,0,input.width,input.height);

		int width,height;

		int scale = UtilWavelet.computeScale(numLevels);
		width = input.width/scale;
		height = input.height/scale;
		width += width%2;
		height += height%2;

		ImageFloat32 levelIn = input.subimage(0,0,width,height);
		ImageFloat32 levelOut = output.subimage(0,0,width,height);
		storage.reshape(width,height);
		inverse1(desc,levelIn,levelOut, storage);

		for( int i = numLevels-1; i >= 1; i-- ) {
			// copy the decoded segment into the input
			levelIn.setTo(levelOut);
			if( i > 1 ) {
				scale /= 2;
				width = input.width/scale;
				height = input.height/scale;
				width += width%2;
				height += height%2;

				storage.reshape(width,height);
				levelIn = input.subimage(0,0,width,height);
				levelOut = output.subimage(0,0,width,height);
			} else {
				levelIn = input;
				levelOut = output;
			}

			storage.reshape(levelIn.width,levelIn.height);
			inverse1(desc,levelIn,levelOut, storage);
		}
	}

	private static ImageFloat32 checkDeclareStorage(int width , int height, ImageFloat32 s, String name ) {
		if( s == null  ) {
			s = new ImageFloat32(width,height);
		} else if( s.width != width || s.height != height ) {
			throw new IllegalArgumentException("'"+name+"' needs to be "+width+"x"+ height+" not "+s.width+"x"+s.height);
		}
		return s;
	}

	private static ImageSInt16 checkDeclareStorage(int width , int height, ImageSInt16 s, String name ) {
		if( s == null  ) {
			s = new ImageSInt16(width,height);
		} else if( s.width != width || s.height != height ) {
			throw new IllegalArgumentException("'"+name+"' needs to be "+width+"x"+ height+" not "+s.width+"x"+s.height);
		}
		return s;
	}
}