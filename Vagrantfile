# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  
  if Vagrant.has_plugin?("vagrant-vbguest")
    config.vbguest.auto_update = false
  end

  BOX = "treehouses/buster64"
  BOX_VERSION = "0.13.25"

  config.vm.define "remote" do |remote|
    remote.vm.box = BOX
    remote.vm.box_version = BOX_VERSION

    remote.vm.hostname = "remote"

    remote.vm.provider "virtualbox" do |vb|
      vb.name = "remote"
      vb.memory="666"
    end

    remote.vm.network "forwarded_port", guest: 22, host: 2222, host_ip: "0.0.0.0", id: "ssh", auto_correct: true

    remote.ssh.shell = "bash -c 'BASH_ENV=/etc/profile exec bash'"
    remote.vm.provision "shell", inline: <<-SHELL
      ln -sr /vagrant /root/remote
      ln -sr /vagrant /home/vagrant/remote
      #windows
      dos2unix /vagrant/*/*/*/* /vagrant/*/*/* /vagrant/*/* /vagrant/*
    SHELL

    # Run binding on each startup make sure the mount is available on VM restart
    remote.vm.provision "shell", run: "always", inline: <<-SHELL
      docker pull codeclimate/codeclimate
    SHELL
  end
end
