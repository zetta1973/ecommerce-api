#!/bin/bash

# Script to download OWASP Dependency Check data for local use
# This script downloads the data once and saves it for future use

set -e

echo "Downloading OWASP Dependency Check data..."

# Create data directory if it doesn't exist
mkdir -p dependency-check-data

# Download the data files
# Note: These URLs may change, check the official documentation for updated URLs
echo "Downloading NVD CVE data..."
curl -L -o dependency-check-data/nvdcve-1.1-modified.json.gz https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.gz

echo "Downloading NVD CVE data (recent)..."
curl -L -o dependency-check-data/nvdcve-1.1-recent.json.gz https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.json.gz

echo "Downloading CISA Known Exploited Vulnerabilities..."
curl -L -o dependency-check-data/known_exploited_vulnerabilities.json https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json

echo "Dependency Check data downloaded successfully!"
echo "You can now commit this data to your repository or use it locally."
