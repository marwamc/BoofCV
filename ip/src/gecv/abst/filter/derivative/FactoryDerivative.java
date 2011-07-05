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

package gecv.abst.filter.derivative;

import gecv.alg.filter.derivative.*;
import gecv.struct.image.ImageBase;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt16;
import gecv.struct.image.ImageUInt8;

import java.lang.reflect.Method;

/**
 * Factory for creating different types of {@link ImageGradient}, which are used to compute
 * the image's derivative.
 *
 * @author Peter Abeles
 */
public class FactoryDerivative {

	public static <I extends ImageBase, D extends ImageBase>
	ImageGradient<I,D> sobel( Class<I> inputType , Class<D> derivType)
	{
		Method m = findDerivative(GradientSobel.class,inputType,derivType);
		return new ImageGradient_Reflection<I,D>(m,true);
	}

	public static <I extends ImageBase, D extends ImageBase>
	ImageGradient<I,D> three( Class<I> inputType , Class<D> derivType)
	{
		Method m = findDerivative(GradientThree.class,inputType,derivType);
		return new ImageGradient_Reflection<I,D>(m,true);
	}

	public static <I extends ImageBase, D extends ImageBase>
	ImageHessianDirect<I,D> hessianDirectThree( Class<I> inputType , Class<D> derivType)
	{
		Method m = findHessian(HessianThree.class,inputType,derivType);
		return new ImageHessianDirect_Reflection<I,D>(m,true);
	}

	public static <I extends ImageBase, D extends ImageBase>
	ImageHessianDirect<I,D> hessianDirectSobel( Class<I> inputType , Class<D> derivType)
	{
		Method m = findHessian(HessianSobel.class,inputType,derivType);
		return new ImageHessianDirect_Reflection<I,D>(m,true);
	}

	public static <D extends ImageBase>
	ImageHessian<D> hessian( Class<?> gradientType , Class<D> derivType ) {
		Method m = findHessianFromGradient(gradientType,derivType);
		return new ImageHessian_Reflection<D>(m,true);
	}

	public static ImageGradient<ImageFloat32,ImageFloat32> gaussian_F32( int radius ) {
		return new ImageGradient_Gaussian_F32(radius);
	}

	public static ImageGradient<ImageFloat32,ImageFloat32> sobel_F32() {
		return sobel(ImageFloat32.class,ImageFloat32.class);
	}

	public static ImageGradient<ImageFloat32,ImageFloat32> three_F32() {
		return three(ImageFloat32.class,ImageFloat32.class);
	}

	public static ImageHessianDirect<ImageFloat32,ImageFloat32> hessianDirectThree_F32() {
		return hessianDirectThree(ImageFloat32.class,ImageFloat32.class);
	}

	public static ImageHessianDirect<ImageFloat32,ImageFloat32> hessianDirectSobel_F32() {
		return hessianDirectSobel(ImageFloat32.class,ImageFloat32.class);
	}

	public static ImageGradient<ImageUInt8, ImageSInt16> sobel_I8() {
		return sobel(ImageUInt8.class,ImageSInt16.class);
	}

	public static ImageGradient<ImageUInt8, ImageSInt16> three_I8() {
		return three(ImageUInt8.class,ImageSInt16.class);
	}

	public static ImageHessianDirect<ImageUInt8, ImageSInt16> hessianDirectThree_I8() {
		return hessianDirectThree(ImageUInt8.class,ImageSInt16.class);
	}

	public static ImageHessianDirect<ImageUInt8, ImageSInt16> hessianDirectSobel_I8() {
		return hessianDirectSobel(ImageUInt8.class,ImageSInt16.class);
	}

	public static ImageHessian<ImageSInt16> hessianThree_I16() {
		return hessian(GradientThree.class,ImageSInt16.class);
	}

	public static ImageHessian< ImageSInt16> hessianSobel_I16() {
		return hessian(GradientSobel.class,ImageSInt16.class);
	}

	public static ImageHessian<ImageFloat32> hessianThree_F32() {
		return hessian(GradientThree.class,ImageFloat32.class);
	}

	public static ImageHessian< ImageFloat32> hessianSobel_F32() {
		return hessian(GradientSobel.class,ImageFloat32.class);
	}

	private static Method findDerivative(Class<?> derivativeClass,
										 Class<?> inputType , Class<?> derivType ) {
		Method m;
		try {
			m = derivativeClass.getDeclaredMethod("process", inputType,derivType,derivType,boolean.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return m;
	}

	private static Method findHessian(Class<?> derivativeClass,
										Class<?> inputType , Class<?> derivType ) {
		Method m;
		try {
			m = derivativeClass.getDeclaredMethod("process", inputType,derivType,derivType,derivType,boolean.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return m;
	}

	private static Method findHessianFromGradient(Class<?> derivativeClass, Class<?> imageType ) {
		String name = derivativeClass.getSimpleName().substring(8);
		Method m;
		try {
			m = HessianFromGradient.class.getDeclaredMethod("hessian"+name, imageType,imageType,imageType,imageType,imageType,boolean.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return m;
	}
}