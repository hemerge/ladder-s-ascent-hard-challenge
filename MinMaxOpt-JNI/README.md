# MinMaxOpt-JNI

## Project Structure
```
MinMaxOpt-JNI/
│── src/
│   ├── MinMaxJNI.java       # Java class for JNI integration
│── native/
│   ├── MinMaxJNI.cpp        # C++ JNI implementation
│   ├── MinMaxJNI.h          # JNI header file (generated)
│── challenge-1/             # Folder containing text files for processing
│── build/
│   ├── libMinMaxJNI.so      # Compiled shared library
│── README.md                # Instructions on building and running the project
```

## Prerequisites
- Java Development Kit (JDK 8+)
- g++ compiler with support for AVX-512
- Linux or WSL (for mmap and AVX-512)
- CMake (optional for managing build process)

## Build Instructions


### Execute the run.sh script.
```sh
chmod +x run.sh
./run.sh
```

## Expected Output
The program processes all files in the `challenge-1/` directory and outputs:
```
Min: <minimum_value>, Max: <maximum_value>
Time Taken: X.XXX seconds
```

## Notes
- Ensure the `challenge-1/` directory exists and contains the text files.
- If you encounter `UnsatisfiedLinkError`, check the library path or rebuild `libMinMaxJNI.so`.
- The program leverages memory-mapped I/O and AVX-512 for high performance.

## Troubleshooting
- Use `export JAVA_HOME=/path/to/java` if JDK is not detected.
- Run `ldd build/libMinMaxJNI.so` to verify dependencies.
- Use `g++ -v` to check AVX-512 support.

## License
MIT License

