package dev.springflow.cli.commands;

public class VersionCommand implements Command {

    @Override
    public void run(String[] args) {
        System.out.println("SpringFlow CLI 0.1.0");
    }
}
