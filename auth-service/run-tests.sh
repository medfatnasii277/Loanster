#!/bin/bash

echo "🧪 Running Auth Service Test Suite"
echo "=================================="

echo
echo "📋 Running Unit Tests..."
mvn test -Dtest="*Test" -DfailIfNoTests=false

echo
echo "🔗 Running Integration Tests..."
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false

echo
echo "📊 Generating Test Coverage Report..."
mvn jacoco:report

echo
echo "✅ Test execution completed!"
echo "📄 Coverage report available at: target/site/jacoco/index.html"
echo "🔍 View test results in: target/surefire-reports/"
