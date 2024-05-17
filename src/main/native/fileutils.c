#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <jni.h>

// ulong 8 byte
// uint 4 byte

//jbyteArray to_jbyteArray(JNIEnv* env, char* buf, int n){
//    setvbuf(stdout, NULL, _IOLBF, 0);
//    printf("Ciao\n");
//    jbyte buf[8];
//    for (int i = 0; i < 8; i++) {
//        buf[i] = (jbyte)((value >> (i * 8)) & 0xFF);
//    }
//    jbyteArray result = (*env)->NewByteArray(env, n);
//    (*env)->SetByteArrayRegion(env, result, 0, n, buf);
//    return result;
//}

//JNIEXPORT jbyteArray JNICALL Java_jgit_FileUtils_test2(JNIEnv* env){
//    int n = 3;
//    char arr[] = {1, 2, 3};
//    jbyte* buf = (jbyte*) malloc(n * sizeof(jbyte));
//    buf[0] = arr[0];
//    buf[1] = arr[1];
//    buf[2] = arr[2];
//    return to_jbyteArray(env, buf, n);
//}

void _stat(char* file, struct stat* st){
    if(stat(file, st) < 0){
        exit(EXIT_FAILURE);
    }
}

ulong get_device_id(char* file){
    struct stat st;
    _stat(file, &st);
    return st.st_dev;
}

ulong get_inode(char* file){
    struct stat st;
    _stat(file, &st);
    return st.st_ino;  
}

uint get_mode(char* file){
    struct stat st;
    _stat(file, &st);
    return st.st_mode;
}

uint get_uid(char* file){
    struct stat st;
    _stat(file, &st);
    return st.st_uid;
}

uint get_gid(char* file){
    struct stat st;
    _stat(file, &st);
    return st.st_gid;
}

JNIEXPORT jlong JNICALL Java_jgit_FileUtils_getDeviceId(JNIEnv* env, jclass class, jstring jfile){
    char* file = (*env)->GetStringUTFChars(env, jfile, NULL);
    ulong device_id = get_device_id(file); 
    (*env)->ReleaseStringUTFChars(env, jfile, file);
    return (jlong) device_id;
}

JNIEXPORT jlong JNICALL Java_jgit_FileUtils_getInode(JNIEnv* env, jclass class, jstring jfile){
    char* file = (*env)->GetStringUTFChars(env, jfile, NULL);
    ulong inode = get_inode(file); 
    (*env)->ReleaseStringUTFChars(env, jfile, file);
    return (jlong) inode;
}

JNIEXPORT jint JNICALL Java_jgit_FileUtils_getMode(JNIEnv* env, jclass class, jstring jfile){
    char* file = (*env)->GetStringUTFChars(env, jfile, NULL);
    uint mode = get_mode(file); 
    (*env)->ReleaseStringUTFChars(env, jfile, file);
    return (jint) mode;
}
JNIEXPORT jint JNICALL Java_jgit_FileUtils_getUid(JNIEnv* env, jclass class, jstring jfile){
    char* file = (*env)->GetStringUTFChars(env, jfile, NULL);
    uint uid = get_uid(file); 
    (*env)->ReleaseStringUTFChars(env, jfile, file);
    return (jint) uid;
}
JNIEXPORT jint JNICALL Java_jgit_FileUtils_getGid(JNIEnv* env, jclass class, jstring jfile){
    char* file = (*env)->GetStringUTFChars(env, jfile, NULL);
    uint gid = get_gid(file); 
    (*env)->ReleaseStringUTFChars(env, jfile, file);
    main();
    return (jint) gid;
}

int main(){
    char* file = "a.txt";
    printf("Device ID: %lu\n", get_device_id(file));
    printf("Inode: %lu\n", get_inode(file));
    printf("Mode: %u\n", get_mode(file));
    printf("UID: %u\n", get_uid(file));
    printf("GID: %u\n", get_gid(file));
}
