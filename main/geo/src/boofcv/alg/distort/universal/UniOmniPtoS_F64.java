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

package boofcv.alg.distort.universal;

import boofcv.alg.distort.radtan.RadialTangential_F64;
import boofcv.struct.calib.CameraUniversalOmni;
import boofcv.struct.distort.Point2Transform3_F64;
import georegression.geometry.GeometryMath_F64;
import georegression.misc.GrlConstants;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import static boofcv.alg.distort.radtan.RemoveRadialNtoN_F64.removeRadial;

/**
 * Backwards project from a distorted 2D pixel to 3D unit sphere coordinate using the {@link CameraUniversalOmni} model.
 *
 * @author Peter Abeles
 */
public class UniOmniPtoS_F64 implements Point2Transform3_F64 {
	double mirrorOffset;
	protected RadialTangential_F64 distortion = new RadialTangential_F64();

	// work space for internal calculations
	private Point2D_F64 p2 = new Point2D_F64();

	private double tol = GrlConstants.DCONV_TOL_A;

		// inverse of camera calibration matrix
	protected DenseMatrix64F K_inv = new DenseMatrix64F(3,3);

	public UniOmniPtoS_F64(CameraUniversalOmni model) {
		this.setModel(model);
	}

	public UniOmniPtoS_F64() {
	}

	public double getTol() {
		return tol;
	}

	public void setTol(double tol) {
		this.tol = tol;
	}

	public void setModel(CameraUniversalOmni model) {
		this.mirrorOffset = (double)model.mirrorOffset;

		distortion.set(model.radial,model.t1,model.t2);

		K_inv.set(0,0, model.fx);
		K_inv.set(1,1, model.fy);
		K_inv.set(0,1, model.skew);
		K_inv.set(0,2, model.cx);
		K_inv.set(1,2, model.cy);
		K_inv.set(2,2,1);

		CommonOps.invert(K_inv);
	}

	@Override
	public void compute(double x, double y, Point3D_F64 out) {
		p2.x = x;
		p2.y = y;

		// initial estimate of undistorted point
		GeometryMath_F64.mult(K_inv, p2, p2);

		// find the undistorted normalized image coordinate
		removeRadial(p2.x, p2.y, distortion.radial, distortion.t1, distortion.t2, p2, tol );

		// put into unit sphere coordinates
		double u = p2.x;
		double v = p2.y;

		// compute adjustment to go from normalized image coordinate to unit sphere
		// X = (u, v , 1)
		// S = (a*u, a*v, a - xi)  and ||S|| = 1
		double a;
		double xi = mirrorOffset;
		if( mirrorOffset == 1.0 ) {
			a = 2.0/(u*u + v*v + 1.0);
		} else {
			double c0 = u*u + v*v + 1.0;
			double c1 = -2.0*xi;
			double c2 = xi*xi - 1;
			a = (-c1 + Math.sqrt(c1*c1 + 4.0*c0*c2))/(2.0*c0);
		}
		out.x = u*a;
		out.y = v*a;
		out.z = a - xi;
	}
}