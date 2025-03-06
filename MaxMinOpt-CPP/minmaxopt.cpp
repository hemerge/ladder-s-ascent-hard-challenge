#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <thread>
#include <atomic>
#include <algorithm>
#include <filesystem>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <climits>
#include <chrono>
#include <mutex>

namespace fs = std::filesystem;

size_t NUM_THREADS = std::thread::hardware_concurrency();
std::atomic<long> globalMin(LONG_MAX);
std::atomic<long> globalMax(LONG_MIN);
std::atomic<size_t> processedFiles(0);
std::mutex printMutex;

void atomic_min(std::atomic<long>& target, long value) {
    long prev = target.load(std::memory_order_relaxed);
    while (value < prev && !target.compare_exchange_weak(prev, value, std::memory_order_relaxed));
}

void atomic_max(std::atomic<long>& target, long value) {
    long prev = target.load(std::memory_order_relaxed);
    while (value > prev && !target.compare_exchange_weak(prev, value, std::memory_order_relaxed));
}

void process_file(const std::string& filename) {
    int fd = open(filename.c_str(), O_RDONLY);
    if (fd == -1) return;

    size_t fileSize = fs::file_size(filename);
    if (fileSize == 0) {
        close(fd);
        return;
    }

    char* mappedData = (char*)mmap(nullptr, fileSize, PROT_READ, MAP_PRIVATE, fd, 0);
    if (mappedData == MAP_FAILED) {
        close(fd);
        return;
    }

    long localMin = LONG_MAX, localMax = LONG_MIN;
    const char* ptr = mappedData;
    const char* end = mappedData + fileSize;

    while (ptr < end) {
        long num = 0;
        bool negative = false;

        if (*ptr == '-') {
            negative = true;
            ++ptr;
        }

        while (ptr < end && *ptr >= '0' && *ptr <= '9') {
            num = num * 10 + (*ptr - '0');
            ++ptr;
        }

        if (negative) num = -num;

        localMin = std::min(localMin, num);
        localMax = std::max(localMax, num);

        while (ptr < end && (*ptr < '0' || *ptr > '9') && *ptr != '-') {
            ++ptr;
        }
    }

    atomic_min(globalMin, localMin);
    atomic_max(globalMax, localMax);

    munmap(mappedData, fileSize);
    close(fd);

    ++processedFiles;
}

void display_progress(size_t totalFiles) {

    while (processedFiles < totalFiles) {
        {
            std::lock_guard<std::mutex> lock(printMutex);
            std::cout << "\rProcessing: "
                      << " " << processedFiles.load() << "/" << totalFiles << " files"
                      << std::flush;
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }

    // Clear progress line after completion
    {
        std::lock_guard<std::mutex> lock(printMutex);
        std::cout << "\rProcessing complete! (" << processedFiles.load() << "/" << totalFiles << ")           \n";
    }
}

int main() {
    auto start = std::chrono::high_resolution_clock::now();

    std::string directory = "challenge-1";
    std::vector<std::string> files;

    for (const auto& entry : fs::directory_iterator(directory)) {
        if (entry.is_regular_file()) {
            files.push_back(entry.path().string());
        }
    }

    size_t totalFiles = files.size();
    if (totalFiles == 0) {
        std::cout << "No files to process.\n";
        return 0;
    }

    size_t numThreads = std::min(NUM_THREADS, totalFiles);
    std::vector<std::thread> threads;
    std::atomic<size_t> fileIndex(0);

    // Start progress animation in a separate thread
    std::thread progressThread(display_progress, totalFiles);

    auto worker = [&]() {
        while (true) {
            size_t idx = fileIndex.fetch_add(1, std::memory_order_relaxed);
            if (idx >= files.size()) break;
            process_file(files[idx]);
        }
    };

    for (size_t i = 0; i < numThreads; ++i)
        threads.emplace_back(worker);

    for (auto& t : threads)
        t.join();

    // Wait for progress animation to finish
    progressThread.join();

    auto end = std::chrono::high_resolution_clock::now();
    double elapsed = std::chrono::duration<double>(end - start).count();

    std::cout << "Min: " << globalMin.load() << ", Max: " << globalMax.load() << std::endl;
    std::cout << "Time Taken: " << elapsed << " seconds" << std::endl;

    return 0;
}
