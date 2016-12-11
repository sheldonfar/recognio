#!/bin/bash
g++ -std=c++0x main.cpp -o main `pkg-config --cflags --libs opencv` -Wno-write-strings -Wno-multichar
