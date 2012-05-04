/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.feature.disparity.impl;

import boofcv.alg.feature.disparity.DisparitySelect;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;
import boofcv.testing.BoofTesting;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestImplDisparityScoreSadRect_U8 {

	Random rand = new Random(234);

	DisparitySelect<int[],ImageUInt8> compDisp = new ImplSelectRectBasicWta_S32_U8();

	/**
	 * Basic generic disparity calculation tests
	 */
	@Test
	public void basicTest() {
		BasicDisparityTests<ImageUInt8,ImageUInt8> alg =
				new BasicDisparityTests<ImageUInt8,ImageUInt8>(ImageUInt8.class) {

					ImplDisparityScoreSadRect_U8<ImageUInt8> alg;

					@Override
					public ImageUInt8 computeDisparity(ImageUInt8 left, ImageUInt8 right ) {
						ImageUInt8 ret = new ImageUInt8(left.width,left.height);

						alg.process(left,right,ret);

						return ret;
					}

					@Override
					public void initialize(int minDisparity , int maxDisparity) {
						alg = new ImplDisparityScoreSadRect_U8<ImageUInt8>(minDisparity,maxDisparity,2,3,compDisp);
					}

					@Override public int getBorderX() { return 2; }

					@Override public int getBorderY() { return 3; }
				};

		alg.checkGradient();
	}

	/**
	 * Compare to a simplistic implementation of stereo disparity.  Need to turn off special
	 * configurations
	 */
	@Test
	public void compareToNaive() {
		int w = 20, h = 25;
		ImageUInt8 left = new ImageUInt8(w,h);
		ImageUInt8 right = new ImageUInt8(w,h);

		ImageUInt8 found = new ImageUInt8(w,h);
		ImageFloat32 expected = new ImageFloat32(w,h);

		GeneralizedImageOps.randomize(left,rand,0,20);
		GeneralizedImageOps.randomize(right,rand,0,20);

		int minDisparity = 0;
		int maxDisparity = 10;
		int radiusX = 3;
		int radiusY = 2;

		ImplDisparityScoreSadRect_U8<ImageUInt8> alg =
				new ImplDisparityScoreSadRect_U8<ImageUInt8>(minDisparity,maxDisparity,radiusX,radiusY,compDisp);
		StereoDisparityWtoNaive<ImageUInt8> naive =
				new StereoDisparityWtoNaive<ImageUInt8>(maxDisparity,radiusX,radiusY);

		alg.process(left,right,found);
		naive.process(left,right,expected);

		BoofTesting.assertEqualsGeneric(found, expected, 1, 1);
	}
}
