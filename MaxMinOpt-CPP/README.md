# High-Performance Min/Max Integer Extraction

## Prerequisites
Ensure you have the following dependencies installed:
- **GCC 9+** or **Clang 10+** (for AVX-512 support)
- **Linux** (recommended) or **WSL** for mmap support

## Build Instructions
### Using g++ (Recommended)
```sh
# Compile with AVX-512 and optimization flags
g++ -std=c++17 -O3 -march=native -pthread minmaxopt.cpp -o minmaxopt
```

## Usage
Run the program by providing the directory containing the text files:
```sh
./minmaxopt
```

### Expected Output
```sh
Min: -999999
Max: 999999
Time Taken: 0.237589 seconds
```

## Performance Optimization
- Uses **memory-mapped files (mmap)** instead of standard file I/O for faster access
- Implements **AVX-512 SIMD instructions** to process multiple integers in parallel
- Uses **multi-threading** to process files in parallel
- Employs **atomic min/max operations** to safely update global values across threads

## Notes
- Ensure your **CPU supports AVX-512** (check with `cat /proc/cpuinfo | grep avx512` on Linux)

## License
This project is released under the **MIT License**.


