package main

import (
	"context"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/lucasx7/LinkSaver/services/extrator-go/internal/extrator"
	"github.com/lucasx7/LinkSaver/services/extrator-go/internal/grpcserver"
	"github.com/lucasx7/LinkSaver/services/extrator-go/internal/httpbuscador"
	"google.golang.org/grpc"
)

func main() {
	porta := os.Getenv("PORTA_GRPC")
	if porta == "" {
		porta = "50051"
	}
	ext, err := extrator.NovoExtrator(httpbuscador.NovoBuscadorHTTP(5 * time.Second))
	if err != nil {
		log.Fatal(err)
	}
	lis, err := net.Listen("tcp", ":"+porta)
	if err != nil {
		log.Fatal(err)
	}
	srv := grpc.NewServer()
	grpcserver.RegistrarServicos(srv, ext)
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	go func() { <-ctx.Done(); srv.GracefulStop() }()
	log.Printf("extrator gRPC ouvindo em :%s", porta)
	if err := srv.Serve(lis); err != nil {
		log.Fatal(err)
	}
}
