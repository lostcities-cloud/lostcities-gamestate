variable "version" {
    type = string
    default = "latest"
}

variable "cpu" {
    type = number
    default = 500
}

variable "memory" {
    type = number
    default = 500
}

variable count {
    type = number
    default = 2
}

variable max_parallel {
    type = number
    default = 2
}

job "gamestate" {
    region = "global"
    datacenters = [ "tower-datacenter"]

    spread {
        attribute = "${node.datacenter}"
        weight    = 100
    }

    group "gamestate" {
        count = var.count

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
                to = 8080
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
                cpu    = var.cpu
                memory = var.memory
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

    update {
        max_parallel     = var.max_parallel
        min_healthy_time = "20s"
        healthy_deadline = "3m"
        auto_revert      = true
        canary           = 1
        auto_promote     = true
    }
}
