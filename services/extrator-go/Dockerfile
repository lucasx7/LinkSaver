FROM golang:1.27 AS builder
WORKDIR /src
RUN curl -sSL "https://github.com/bufbuild/buf/releases/download/v1.75.0/buf-$(uname -s)-$(uname -m)" -o /usr/local/bin/buf \
 && chmod +x /usr/local/bin/buf \
 && go install google.golang.org/protobuf/cmd/protoc-gen-go@latest \
 && go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
COPY services/extrator-go/go.mod services/extrator-go/go.sum ./services/extrator-go/
RUN cd services/extrator-go && go mod download
COPY protos ./protos
COPY services/extrator-go ./services/extrator-go
RUN cd protos && buf generate
RUN cd services/extrator-go && CGO_ENABLED=0 go build -o /extrator ./cmd/extrator

FROM alpine:3.21
RUN apk add --no-cache ca-certificates
COPY --from=builder /extrator /usr/local/bin/extrator
EXPOSE 50051
HEALTHCHECK --interval=5s --timeout=3s --retries=5 CMD nc -z 127.0.0.1 50051 || exit 1
CMD ["/usr/local/bin/extrator"]
