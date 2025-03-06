# High-Performance Min/Max Integer Extraction

## Prerequisites
Ensure you have the following dependencies installed:
- **GCC 9+** or **Clang 10+** (for AVX-512 support)
- **Linux** (recommended) or **WSL** for mmap support

## Build Instructions
### Using g++ (Recommended)
```sh
g++ -std=c++17 -O3 -march=native -pthread minmaxopt.cpp -o minmaxopt
```

## Usage
Run the program by providing the directory containing the text files:
```sh
./minmaxopt
```

### Sample Output
```sh
Processing complete! (15000/15000)           
Min: -9223371980708175790, Max: 9223372023241467945
Time Taken: 1.10702 seconds
```

## Performance Optimization
- Uses **memory-mapped files (mmap)** instead of standard file I/O for faster access
- Uses std::min / std::max on a per-thread basis instead of SIMD registers.
- Uses **multi-threading** to process files in parallel
- Employs **atomic min/max operations** to safely update global values across threads

## Notes
- **NOT REQUIRED ANYMORE** Ensure your **CPU supports AVX-512** (check with `cat /proc/cpuinfo | grep avx512` on Linux)

## License
This project is released under the **MIT License**.


