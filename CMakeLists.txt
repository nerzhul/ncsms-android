cmake_minimum_required(VERSION 3.5.0)

set (SRC_FILES
	${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/httpclient.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/json.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/nativesms.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/smsbuffer.cpp
)

find_library(LOGGING_LIBRARY log)

add_library(nativesms SHARED ${SRC_FILES})

target_link_libraries(nativesms ${LOGGING_LIBRARY})