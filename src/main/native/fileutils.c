#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <jni.h>

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
    return (jint) gid;
}
