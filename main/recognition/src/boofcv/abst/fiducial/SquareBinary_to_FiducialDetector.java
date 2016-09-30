/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.fiducial;

import boofcv.alg.fiducial.square.DetectFiducialSquareBinary;
import boofcv.struct.calib.CameraPinholeRadial;
import boofcv.struct.image.ImageGray;

/**
 * Wrapper around {@link DetectFiducialSquareBinary} for {@link FiducialDetector}
 *
 * @author Peter Abeles
 */
public class SquareBinary_to_FiducialDetector<T extends ImageGray>
	extends BaseSquare_FiducialDetector<T,DetectFiducialSquareBinary<T>>
{
	double targetWidth;

	public SquareBinary_to_FiducialDetector(DetectFiducialSquareBinary<T> alg, double targetWidth) {
		super(alg);
		this.targetWidth = targetWidth;
	}

	@Override
	public void setIntrinsic(CameraPinholeRadial intrinsic) {
		alg.setLengthSide(targetWidth);
		super.setIntrinsic(intrinsic);
	}

	@Override
	public double getWidth(int which) {
		return targetWidth;
	}
}
