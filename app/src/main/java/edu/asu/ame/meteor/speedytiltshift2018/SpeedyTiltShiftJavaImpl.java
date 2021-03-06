package edu.asu.ame.meteor.speedytiltshift2018;

import java.util.logging.Level;
import java.util.logging.Logger;

/*
This class contains the methods to apply the Gaussian blur effects,
    given the sigma values and y-coordinates of regions to denote depth.
The final result is an array of pixel values representing the blurred image transformation.
The 2-step weight vector approach is applied on the input pixels
    to obtain the blur effects.
A Gaussian-weighted combination of nearby input pixel values is computed
    to assign a new (R,G,B) value to each pixel.
 */

public class SpeedyTiltShiftJavaImpl {

 /* method to compute Gaussian Kernel
    assigns computed values to the array reference provided
 */
    public static void create_gaussian_kernel(int r, float sigma, float[] gaussianKernel){
        for(int i=0; i<gaussianKernel.length; i++){
            float temp = -1*((-1*r)+i)*((-1*r)+i);
            temp /= (2*sigma*sigma);
            temp = (float)Math.exp(temp);
            temp /= Math.sqrt(2*Math.PI*sigma*sigma);
            gaussianKernel[i] = (temp);
        }
    }

    /*
    method to copy the pixels when sigma<0.6
    copies the input array to the output array
     */
    public static void copy_row(int[] pixels, int[] output, int j, int width){
        for(int i=0; i<width; i++){
            int B = pixels[j*width+i]&0xff;
            int G = (pixels[j*width+i]>>8)&0xff;
            int R = (pixels[j*width+i]>>16)&0xff;
            int A = 0xff;

            int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
            output[j*width+i]=color;
        }
    }

    /*
    method to calculate intermediate value using weight vector approach(fast approach)
    returns an integer value representing the q(y,x) value
     */
    public static int qvector(int[] pixels, int x, int y, int r, float[] kern, int height, int width) {
        int len = (2*r)+1;
        int q;

        int tempB = 0, tempR = 0, tempG = 0;
        for(int i=0; i<len; i++){
            if( (y-r+i) < 0 || (y-r+i) >= height){
                continue;
            }else {
                q = pixels[(y - r + i) * width + x];
            }
            int B = q&0xff;
            int G = (q>>8)&0xff;
            int R = (q>>16)&0xff;

            tempB += B*kern[i];
            tempG += G*kern[i];
            tempR += R*kern[i];
        }
        int A = 0xff;
        return (A & 0xff) << 24 | (tempR & 0xff) << 16 | (tempG & 0xff) << 8 | (tempB & 0xff);
    }

    /*
     method to calculate the final output pixel value using weight vector approach
    returns an integer representing the final Gaussian-weighted pixel value
     */
    public static int pvector(int[] pixels, int x, int y, int r, float[] kern, int height, int width){
        int len = (2*r)+1;
        int p,q;

        float tempB = 0, tempR = 0, tempG = 0;
        for(int i=0; i<len; i++){
            if( (x-r+i) < 0 || (x-r+i) >= width){
                continue;
            }else {
                // Calling in qvector function to calculate intermediate vector
                q = qvector(pixels, x-r+i, y, r, kern, height, width);
            }
            int B = q&0xff;
            int G = (q>>8)&0xff;
            int R = (q>>16)&0xff;
            tempB += (B*kern[i]);
            tempG += (G*kern[i]);
            tempR += (R*kern[i]);
        }
        int A = 0xff;
        p = (A & 0xff) << 24 | (Math.round(tempR) & 0xff) << 16 | (Math.round(tempG) & 0xff) << 8 | (Math.round(tempB) & 0xff);
        return p;
    }

    /*
    method to compute the output image to simulate focal blur effects
    given the sigma far/near values and the y-coordinates to apply the progressive blur effects
     */
    public static void tiltshiftJavaImpl(int[] pixels, int[] outputPixels, int width, int height, float sigma_far, float sigma_near, int a0, int a1, int a2, int a3) {
        float sigma=0;
        int r_prev = 0, r_new;
        float[] kern = null;

        for (int j=0;j<height;j++){
            if(j<a0){
                sigma = sigma_far;
            } else if(j<a1){
                sigma = (float)(a1-j)/(float)(a1-a0);
                sigma *= sigma_far;
            } else if(j<a2){
                sigma = 0.5f;
            } else if(j<a3){
                sigma = (float)(j-a2)/(float)(a3-a2);
                sigma *= sigma_near;
            } else{
                sigma = sigma_near;
            }

            if(sigma<0.6){
                // Calling in copy_row function to copy the pixel values when sigma < 0.6 (Gaussian blur should not be applied)
                copy_row(pixels, outputPixels, j, width);
                continue;
            }

            r_new= Math.round((float)Math.ceil(2*sigma));

            if(r_new!=r_prev){
                kern = new float[(2*r_new)+1];// = NULL;
                create_gaussian_kernel(r_new, sigma, kern);
                r_prev = r_new;
            }

            for (int i=0;i<width;i++) {
                int p = pvector(pixels, i, j, r_new, kern, height, width);
                outputPixels[j*width+i]= p;
            }
        }
    }
}
