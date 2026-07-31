#ifndef MINI_SYNTH_LOCKFREEQUEUE_H
#define MINI_SYNTH_LOCKFREEQUEUE_H

#include <atomic>
#include <vector>

template <typename T>
class LockFreeQueue {
public:
    explicit LockFreeQueue(size_t capacity)
        : mCapacity(capacity), mBuffer(capacity), mHead(0), mTail(0) {
        // Ensure capacity is a power of two for bitwise wrap
        mMask = capacity - 1;
    }

    bool push(const T& item) {
        size_t head = mHead.load(std::memory_order_relaxed);
        size_t nextHead = (head + 1) & mMask;
        if (nextHead == mTail.load(std::memory_order_acquire)) {
            return false; // Full
        }
        mBuffer[head] = item;
        mHead.store(nextHead, std::memory_order_release);
        return true;
    }

    bool pop(T& item) {
        size_t tail = mTail.load(std::memory_order_relaxed);
        if (tail == mHead.load(std::memory_order_acquire)) {
            return false; // Empty
        }
        item = mBuffer[tail];
        mTail.store((tail + 1) & mMask, std::memory_order_release);
        return true;
    }

    size_t size() const {
        size_t head = mHead.load(std::memory_order_acquire);
        size_t tail = mTail.load(std::memory_order_acquire);
        if (head >= tail) return head - tail;
        return mCapacity - (tail - head);
    }

    void clear() {
        mHead.store(0, std::memory_order_relaxed);
        mTail.store(0, std::memory_order_release);
    }

private:
    size_t mCapacity;
    size_t mMask;
    std::vector<T> mBuffer;
    std::atomic<size_t> mHead;
    std::atomic<size_t> mTail;
};

#endif //MINI_SYNTH_LOCKFREEQUEUE_H
