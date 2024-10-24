variable "version" {
    type = string
    default = "latest"
}

job "gamestate" {
    region = "global"
    datacenters = [ "tower-datacenter"]

    update {
        max_parallel = 2
    }

    group "gamestate" {
        count = 2

        restart {
            attempts = 10
            interval = "5m"
            delay    = "25s"
            mode     = "delay"
        }

        network {
            mode = "bridge"

            port "management-port" {
                to     = 4452
            }

            port "service-port" {
                to = 8092
            }
        }

        service {
            name = "gamestate-service"
            port = "service-port"
            tags = ["urlprefix-/api/gamestate"]
            #address_mode = "alloc"

            check {
                type = "http"
                port = "management-port"
                path = "/management/gamestate/actuator/health"
                interval = "30s"
                timeout  = "10s"
                failures_before_critical = 20
                failures_before_warning = 10
            }
        }

        service {
            name = "gamestate-management"
            port = "management-port"
            tags = ["urlprefix-/management/gamestate/actuator"]

            check {
                type = "http"
                port = "management-port"
                path = "/management/gamestate/actuator/health"
                interval = "30s"
                timeout  = "10s"
                failures_before_critical = 20
                failures_before_warning = 10
            }
        }

        task "gamestate" {
            driver = "podman"


            env {
                SPRING_PROFILES_ACTIVE = "dev"
            }

            resources {
                cpu    = 100
                memory = 500
            }

            config {
                force_pull = true
                image = "ghcr.io/lostcities-cloud/lostcities-gamestate:${var.version}"
                ports = ["service-port", "management-port"]
                logging = {
                    driver = "nomad"
                }
            }

            template {
                data        = <<EOF
{{ range service "postgres" }}
POSTGRES_IP="{{ .Address }}"
{{ else }}
{{ end }}
{{ range service "redis" }}
REDIS_IP="{{ .Address }}"
{{ else }}
{{ end }}
{{ range service "rabbitmq" }}
RABBITMQ_IP="{{ .Address }}"
{{ else }}
{{ end }}
EOF
                change_mode = "restart"
                destination = "local/discovery.env"
                env         = true
            }
        }
    }
}
