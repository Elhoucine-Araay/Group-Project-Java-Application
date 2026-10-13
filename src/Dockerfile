FROM node:22

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

ENV PORT=7643

EXPOSE 7643

CMD [ "npm", "start" ]
