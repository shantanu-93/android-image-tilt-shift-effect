#include <jni.h>
#include <arm_neon.h>
#include <iostream>
#include <string>
#include <cpu-features.h>
#include <cmath>
//for logging
#include <android/log.h>
#define LOG_TAG "testjni"
#define PI 3.14

using namespace std;
//__android_log_print(ANDROID_LOG_INFO, "TAG", "%d, %d", width, height);
// C++ implementation functions declared for calculating Gaussian Kernel, Intermediate and Final matrix
float * create_gaussian_kernel(int, float);
void copy_row(jint *, jint *, int, jint);
int pvector(jint *, int, int, int, float *, int, int);
int qvector(jint *, int, int, int, float *, int, int);

extern "C"
JNIEXPORT jint JNICALL
Java_edu_asu_ame_meteor_speedytiltshift2018_SpeedyTiltShift_tiltshiftcppnative(JNIEnv *env,
                                                                               jclass instance,
                                                                               jintArray inputPixels_,
                                                                               jintArray outputPixels_,
                                                                               jint width,
                                                                               jint height,
                                                                               jfloat sigma_far,
                                                                               jfloat sigma_near,
                                                                               jint a0, jint a1,
                                                                               jint a2, jint a3) {
    jint *pixels = env->GetIntArrayElements(inputPixels_, NULL);
    jint *outputPixels = env->GetIntArrayElements(outputPixels_, NULL);
    long length = env->GetArrayLength(inputPixels_);

//    __android_log_print(ANDROID_LOG_INFO, "TAG", "sigma_far = %f, sigma_near = %f\n", sigma_far, sigma_near);
//    __android_log_print(ANDROID_LOG_INFO, "TAG", "a0= %d, a1 = %d, a2 = %d, a3 = %d\n", a0, a1, a2, a3);
//    __android_log_print(ANDROID_LOG_INFO, "TAG", "width = %d, length = %d\n", width, height);

    float *kern = NULL;
    float sigma=0.0;
    int r_prev = 0, r_new = 0;
    for (int j=0;j<height;j++){
        //calculate sigma value based on the pixel position
        if(j<a0){
            sigma = sigma_far;
        }
        else if(j<a1){
            sigma = (sigma_far*(a1-j))/(a1-a0);
        }
        else if(j>=a1 && j<a2){
            sigma = 0.5;
        }
        else if(j<a3){
            sigma = (sigma_near*(j-a2))/(a3-a2);
        }
        else if(j<height){
            sigma = sigma_near;
        }
        if(sigma<0.6){
            // Calling in copy_row function to copy the pixel values when sigma < 0.6 (Gaussian blur should not be applied)
            copy_row(pixels, outputPixels, j, width);
            continue;
        }
        r_new= int(ceil(2*sigma));
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "height = %d, current = %d", height, j);
        if(r_new!=r_prev){
            delete[] kern;
            //Calling in create_gaussian_kernel to calculate the Gaussian Kernel Matrix
            kern = create_gaussian_kernel(r_new, sigma);
            r_prev = r_new;
        }
        for (int i=0;i<width;i++) {
            // Iterating over width of the image to obtain the final output pixel
            int p = pvector(pixels, i, j, r_new, kern, height, width);
            //int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
            outputPixels[j*width+i]= p;
        }
    }
    delete kern;
    env->ReleaseIntArrayElements(inputPixels_, pixels, 0);
    env->ReleaseIntArrayElements(outputPixels_, outputPixels, 0);
    __android_log_print(ANDROID_LOG_INFO, "TAG", "CPP operation finished!\n");
    return 0;
}

// C++ implementation function to calculate intermediate vector using weight vector approach(fast approach)
int qvector(int * pixels, int x, int y, int r, float * kern, int height, int width) {
    //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside q");
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
    int colorq = (A & 0xff) << 24 | (tempR & 0xff) << 16 | (tempG & 0xff) << 8 | (tempB & 0xff);
    return colorq;
}
//C++ implementation function to calculate the final output vector using weight vector approach(fast approach)
int pvector(int * pixels, int x, int y, int r, float * kern, int height, int width){
    //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside p1");
    int len = (2*r)+1;
    int p,q;
    float tempB = 0, tempR = 0, tempG = 0;
    for(int i=0; i<len; i++){
        if( (x-r+i) < 0 || (x-r+i) >= width){
            //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside p2");
            continue;
        }else {
            // Calling in qvector function to calculate intermediate vector
            q = qvector(pixels, x-r+i, y, r, kern, height, width);
        }
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside p3");
        int B = q&0xff;
        int G = (q>>8)&0xff;
        int R = (q>>16)&0xff;
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside p4");
        tempB += (B*kern[i]);
        tempG += (G*kern[i]);
        tempR += (R*kern[i]);
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "inside p5");
    }
    int A = 0xff;
    p = (A & 0xff) << 24 | (int(tempR) & 0xff) << 16 | (int(tempG) & 0xff) << 8 | (int(tempB) & 0xff);
    return p;
}

// Function to copy the pixel values when sigma < 0.6 (Gaussian kernel not applied)
void copy_row(jint * pixels, jint * output, int j, jint width){
    for(int i=0; i<width; i++){
        int B = pixels[j*width+i]&0xff;
        int G = (pixels[j*width+i]>>8)&0xff;
        int R = (pixels[j*width+i]>>16)&0xff;
        int A = 0xff;

        int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        output[j*width+i]=color;
    }
}

// Function to calculate Gaussian Kernel matrix
float * create_gaussian_kernel(int r, float sigma){
    //__android_log_print(ANDROID_LOG_INFO, "TAG", "function called!");
    int len = (2*r)+1;
    float * G = new float [len];
    for(int i=0; i<len; i++){
        float temp = -1*((-1*r)+i)*((-1*r)+i);
        temp /= (2*sigma*sigma);
        temp = exp(temp);
        temp /= sqrt(2*PI*sigma*sigma);
        G[i] = (temp);
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "kern[%d] = %f", i, G[i]);
    }
    return G;
}

//// NEON implementation function to print the pixels
//void print_func(uint16x8_t data){
//    //__android_log_print(ANDROID_LOG_INFO, "TAG", "print");
//    uint16_t p[8];
//
//    vst1q_u16(p, data);
//
//    for(int i=0; i<8; i++){
//        __android_log_print(ANDROID_LOG_INFO, "TAG", "element[%d] : %d\n", i, p[i]);
//    }
//}

//Splitting channels into separate vectors
uint32x4_t getARGB(int* pixel){
    uint32x4_t channels;

    uint32_t B = (uint32_t)((*pixel)&0xff);
    uint32_t G = (uint32_t)((*pixel>>8)&0xff);
    uint32_t R = (uint32_t)((*pixel>>16)&0xff);
    uint32_t A = (uint32_t)((*pixel>>24)&0xff);

    channels = vsetq_lane_u32(A,channels,3);
    channels = vsetq_lane_u32(R,channels,2);
    channels = vsetq_lane_u32(G,channels,1);
    channels = vsetq_lane_u32(B,channels,0);
    return channels;
}

// NEON implementation function to calculate intermediate vector using weight vector approach(fast approach)
float32x4_t neon_qvector(int * pixels, int x, int y, int r, float * kern, int height, int width) {
    int len = (2*r)+1;
    float32x4_t q = vdupq_n_f32(0);
    uint32x4_t p;
    for(int i=0; i<len; i++){
        if( (y-r+i) < 0 || (y-r+i) >= height){
            continue;
        }
        else {
            p = getARGB(pixels+ (((y-r+i)*width)+x) );
        }
        float32x4_t float_p = vcvtq_f32_u32(p);
        q = vmlaq_n_f32(q, float_p, kern[i]);
    }
    return q;
}

// NEON implementation function to calculate the final output vector using weight vector approach(fast approach)
void neon_pvector(int * pixels, int * outputPixels, int x, int y, int r, float * kern, int height, int width){
    int len = (2*r)+1;
    float32x4_t q;
    float32x4_t p = vdupq_n_f32(0);
    for(int i=0; i<len; i++){
        if( (x-r+i) < 0 || (x-r+i) >= width){
            continue;
        }
        else {
            q = neon_qvector(pixels, x - r + i, y, r, kern, height, width);
        }
        p = vmlaq_n_f32(p, q, kern[i]);
    }

    uint32x4_t result = vcvtq_u32_f32(p);
    int A = vgetq_lane_u32(result, 3);
    int R = vgetq_lane_u32(result, 2);
    int G = vgetq_lane_u32(result, 1);
    int B = vgetq_lane_u32(result, 0);
    outputPixels[y*width + x] = (0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
    return;
}

extern "C"
JNIEXPORT jint JNICALL
Java_edu_asu_ame_meteor_speedytiltshift2018_SpeedyTiltShift_tiltshiftneonnative(JNIEnv *env,
                                                                                jclass instance,
                                                                                jintArray inputPixels_,
                                                                                jintArray outputPixels_,
                                                                                jint width,
                                                                                jint height,
                                                                                jfloat sigma_far,
                                                                                jfloat sigma_near,
                                                                                jint a0, jint a1,
                                                                                jint a2, jint a3) {
    jint *pixels = env->GetIntArrayElements(inputPixels_, NULL);
    jint *outputPixels = env->GetIntArrayElements(outputPixels_, NULL);

    //__android_log_print(ANDROID_LOG_INFO, "TAG", "width = %d, length = %d", width, height);
    float *kern = NULL;
    float sigma=0.0;
    int r_prev = 0, r_new = 0;

    //calculate sigma value based on the pixel position
    for (int j=0;j<height;j++){
        if(j<a0){
            sigma = sigma_far;
        }
        else if(j<a1){
            sigma = (sigma_far*(a1-j))/(a1-a0);
        }
        else if(j>=a1 && j<a2){
            sigma = 0.5;
        }
        else if(j<a3){
            sigma = (sigma_near*(j-a2))/(a3-a2);
        }
        else if(j<height){
            sigma = sigma_near;
        }
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "sigma = %f", sigma);
        if(sigma<0.6){
            // Calling in copy_row function to copy the pixel values when sigma < 0.6 (Gaussian blur should not be applied)
            copy_row(pixels, outputPixels, j, width);
            continue;
        }
        r_new= int(ceil(2*sigma));
        //__android_log_print(ANDROID_LOG_INFO, "TAG", "height = %d, current = %d", height, j);
        if(r_new!=r_prev){
            delete[] kern;
            //Calling in create_gaussian_kernel to calculate the Gaussian Kernel Matrix
            kern = create_gaussian_kernel(r_new, sigma);
            r_prev = r_new;
        }
        for (int i=0;i<width;i++) {
            // Iterating over width of the image to obtain the final output pixels
           neon_pvector(pixels, outputPixels, i, j, r_prev, kern, height, width);
        }
    }
    env->ReleaseIntArrayElements(inputPixels_, pixels, 0);
    env->ReleaseIntArrayElements(outputPixels_, outputPixels, 0);
    __android_log_print(ANDROID_LOG_INFO, "TAG", "Neon operation finished!\n");
    return 0;
}