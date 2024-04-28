# Build stage
FROM node:18 AS build

WORKDIR /app

# Copy package.json and package-lock.json to the container
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy the rest of the application code
COPY . .

# Build the React app
RUN npm run --openssl-legacy-provider build


# Expose port 80
EXPOSE 3000

CMD ["npm", "--openssl-legacy-provider", "start"]